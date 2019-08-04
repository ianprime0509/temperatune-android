package com.ianprime0509.temperatune.util

infix fun Int.positiveRem(other: Int) = (this % other + other) % other

infix fun Double.positiveRem(other: Int) = (this % other + other) % other
