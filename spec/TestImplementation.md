### Test Implementation
```typescript
// Example test suite
describe('ShoppingListOptimizer', () => {
  it('should minimize total cost across multiple stores', async () => {
    const items = [
      { productId: '1', quantity: 2 },
      { productId: '2', quantity: 1 }
    ]
    
    const result = await optimizer.optimize(
      items,
      OptimizationStrategy.COST_MINIMUM
    )
    
    expect(result.totalCost).toBeLessThan(manualTotal)
    expect(result.stores).toHaveLength(2)
    expect(result.savings).toBeGreaterThan(0)
  })
})
```

## 🔄 DevOps & Deployment

### CI/CD Pipeline
```yaml
name: Deploy Pipeline

on:
  push:
    branches: [main, develop]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Install dependencies
        run: npm ci
      - name: Run tests
        run: npm test
      - name: SonarCloud scan
        uses: SonarSource/sonarcloud-github-action@master

  build:
    needs: test
    strategy:
      matrix:
        platform: [ios, android, web, backend]
    steps:
      - name: Build ${{ matrix.platform }}
        run: npm run build:${{ matrix.platform }}

  deploy:
    needs: build
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to production
        run: |
          kubectl apply -f k8s/
          helm upgrade alles-teuer ./charts
```

### Infrastructure as Code
```terraform
# AWS Infrastructure
module "alles_teuer" {
  source = "./modules/app"
  
  # RDS PostgreSQL
  database = {
    engine         = "postgres"
    version        = "15.4"
    instance_class = "db.t3.medium"
    storage        = 100
  }
  
  # EKS Cluster
  kubernetes = {
    version    = "1.28"
    node_count = 3
    node_type  = "t3.large"
  }
  
  # Redis Cache
  redis = {
    node_type = "cache.t3.micro"
    replicas  = 2
  }
  
  # S3 Buckets
  storage = {
    receipts_bucket = "alles-teuer-receipts"
    images_bucket   = "alles-teuer-images"
  }
}
```

## 📁 Project Structure

```
alles-teuer/
├── apps/
│   ├── ios/                 # iOS App (Swift/SwiftUI)
│   ├── android/              # Android App (Kotlin)
│   ├── web/                  # Web App (Next.js)
│   └── backend/              # Backend Services (NestJS)
├── packages/
│   ├── shared/               # Shared TypeScript code
│   ├── ui/                   # Shared UI components
│   ├── api-client/           # Generated API client
│   └── database/             # Prisma schema & migrations
├── services/
│   ├── auth/                 # Authentication service
│   ├── product/              # Product management
│   ├── price/                # Price tracking
│   ├── ocr/                  # OCR processing
│   └── optimizer/            # Optimization engine
├── infrastructure/
│   ├── terraform/            # IaC definitions
│   ├── k8s/                  # Kubernetes manifests
│   └── docker/               # Dockerfiles
├── docs/
│   ├── api/                  # API documentation
│   ├── architecture/         # Architecture decisions
│   └── guides/               # Development guides
└── scripts/
    ├── setup.sh              # Development setup
    └── deploy.sh             # Deployment scripts
```

## 🤖 AI Assistant Implementation Guide

### Step-by-Step Instructions for AI Implementation

#### 1. Environment Setup
```bash
# Clone and setup
git clone https://github.com/mpwg/AllesTeurer.git
cd AllesTeurer

# Install dependencies
npm install

# Setup environment variables
cp .env.example .env
# Edit .env with necessary API keys and configs

# Initialize database
npm run db:setup
npm run db:migrate
```

#### 2. Backend Implementation Order
1. **Authentication Service** - Start with user management
2. **Product Service** - Core domain logic
3. **Price Service** - Price tracking and comparison
4. **OCR Service** - Receipt processing
5. **Optimizer Service** - Shopping list optimization

#### 3. Frontend Implementation Order
1. **Navigation & Layout** - Base structure
2. **Authentication Flow** - Login/Register
3. **Product Search** - Core feature
4. **Shopping Lists** - List management
5. **Receipt Scanning** - Camera integration
6. **Analytics Dashboard** - Data visualization

#### 4. Integration Points
```typescript
// Key integration interfaces
interface PriceProvider {
  getName(): string
  searchProducts(query: string): Promise<Product[]>
  getPrices(productId: string): Promise<Price[]>
}

interface OCRProvider {
  processImage(image: Buffer): Promise<string>
  parseReceipt(text: string): Promise<Receipt>
}

interface OptimizationEngine {
  optimize(items: Item[], strategy: Strategy): Promise<Result>
}
```

## 📝 Additional Notes for AI Implementation

### Code Generation Guidelines
- Use TypeScript with strict mode enabled
- Follow Clean Architecture principles
- Implement comprehensive error handling
- Add JSDoc comments for all public APIs
- Write tests alongside implementation
- Use dependency injection for testability

### Common Pitfalls to Avoid
- Don't hardcode API endpoints
- Avoid synchronous blocking operations
- Don't store sensitive data in plain text
- Avoid N+1 query problems
- Don't skip input validation

### Performance Considerations
- Implement pagination for all list endpoints
- Use database indexes strategically
- Cache frequently accessed data
- Optimize images before storage
- Use CDN for static assets

## 🎯 Definition of Done

A feature is considered complete when:
- [ ] Code is written and reviewed
- [ ] Unit tests pass with >80% coverage
- [ ] Integration tests are implemented
- [ ] API documentation is updated
- [ ] UI is responsive on all platforms
- [ ] Performance meets defined KPIs
- [ ] Security scan passes
- [ ] Feature is deployed to staging
- [ ] Product owner approves

---

**This document serves as the single source of truth for the Alles Teuer project implementation. AI assistants should reference this document when generating code and making architectural decisions.**
