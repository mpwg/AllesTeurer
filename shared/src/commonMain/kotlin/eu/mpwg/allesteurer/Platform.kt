package eu.mpwg.allesteurer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform