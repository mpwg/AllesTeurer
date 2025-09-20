#!/usr/bin/env ruby
require 'fileutils'

# Script to fix Xcode project shellScript array format issue
# This converts shellScript arrays to strings for xcodeproj compatibility

project_file = "iosApp.xcodeproj/project.pbxproj"

unless File.exist?(project_file)
  puts "Error: #{project_file} not found!"
  exit 1
end

puts "📄 Reading project file..."
content = File.read(project_file)

# Create backup
backup_file = "#{project_file}.backup.#{Time.now.to_i}"
File.write(backup_file, content)
puts "💾 Created backup: #{backup_file}"

# Find and fix shellScript arrays - more flexible regex
fixed_content = content.gsub(/shellScript = \(\s*\n(.*?)\s*\);/m) do |match|
  # Extract the array content
  array_content = $1
  
  # Parse each line and extract quotes content, handling escaped quotes
  lines = []
  array_content.scan(/\s*"((?:[^"\\]|\\.)*)"\s*,?\s*/) do |line_content|
    # Unescape quotes and handle empty lines
    line = line_content[0].gsub(/\\"/, '"')
    lines << line
  end
  
  # Join lines with \n and create the string format
  shell_script_string = lines.join("\\n")
  
  puts "🔧 Converting shellScript array to string"
  puts "   Lines found: #{lines.length}"
  lines.each_with_index { |line, i| puts "   #{i+1}: #{line}" }
  
  "shellScript = \"#{shell_script_string}\";"
end

if fixed_content != content
  File.write(project_file, fixed_content)
  puts "✅ Fixed shellScript arrays in project file"
  puts "🔄 You should now be able to run fastlane without the xcodeproj error"
else
  puts "ℹ️  No shellScript arrays found that needed fixing"
end