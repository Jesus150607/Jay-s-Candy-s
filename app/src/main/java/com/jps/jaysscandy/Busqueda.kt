package com.jps.jaysscandy

import java.text.Normalizer

/** Minúsculas y sin acentos, para que "galleta" encuentre "Galléta" y "CHOCOLATE" encuentre "chocolate". */
fun String.normalizarTexto(): String =
    Normalizer.normalize(this.trim().lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
