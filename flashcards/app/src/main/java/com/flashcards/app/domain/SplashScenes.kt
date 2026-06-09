package com.flashcards.app.domain

data class SplashScene(
    val assetFile: String,
    val credit: String,
    val essence: String,
    val headline: String,
    val subline: String,
)

object SplashScenes {
    val all = listOf(
        SplashScene(
            assetFile = "sunrise-alps.jpg",
            credit = "Sunrise over the Alps · Unsplash",
            essence = "New beginning",
            headline = "Rise into who you're becoming.",
            subline = "The mountain doesn't ask who you were yesterday. It only asks if you're willing to climb again today.",
        ),
        SplashScene(
            assetFile = "golden-dawn.jpg",
            credit = "Valley at sunrise · Unsplash",
            essence = "Fresh start",
            headline = "Light finds you, even after the longest night.",
            subline = "Every sunrise is proof that nothing stays finished forever — including the person you're allowed to become.",
        ),
        SplashScene(
            assetFile = "seedling.jpg",
            credit = "Seedling in morning light · Unsplash",
            essence = "Quiet growth",
            headline = "Small beginnings change everything.",
            subline = "A single seed doesn't rush. It roots, then rises. Your knowledge grows the same way — one card, one day.",
        ),
        SplashScene(
            assetFile = "open-road.jpg",
            credit = "Road beside still water · Unsplash",
            essence = "Open path",
            headline = "The road ahead doesn't know your past.",
            subline = "You don't need permission to turn the page. You only need the courage to take the first step forward.",
        ),
        SplashScene(
            assetFile = "ocean-dawn.jpg",
            credit = "Sea at golden hour · Unsplash",
            essence = "Horizon calling",
            headline = "The horizon is an invitation, not a limit.",
            subline = "Waves erase the shore and draw it anew. You can let go of what didn't stick — and begin again.",
        ),
        SplashScene(
            assetFile = "ocean-waves.jpg",
            credit = "Waves at dawn · Unsplash",
            essence = "Transformation",
            headline = "Change doesn't have to be loud to be real.",
            subline = "What feels like starting over is often becoming. Every review is you choosing growth over forgetting.",
        ),
        SplashScene(
            assetFile = "open-book.jpg",
            credit = "Pages waiting to be read · Unsplash",
            essence = "Turn the page",
            headline = "Today you write a new chapter.",
            subline = "The stories you carry don't end where you left off. Pick up the pen. Your future self is reading along.",
        ),
        SplashScene(
            assetFile = "forest-mist.jpg",
            credit = "Forest path in morning mist · Unsplash",
            essence = "Second chance",
            headline = "Mist clears. The path appears.",
            subline = "When you can't see far ahead, you only need to see the next step. That's enough to begin again.",
        ),
    )

    fun pickRandom(avoidIndex: Int): Int {
        if (all.size <= 1) return 0
        var i: Int
        do {
            i = (all.indices).random()
        } while (i == avoidIndex)
        return i
    }
}
