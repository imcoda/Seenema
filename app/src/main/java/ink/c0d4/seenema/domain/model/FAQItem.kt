package ink.c0d4.seenema.domain.model

data class FAQItem(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)
