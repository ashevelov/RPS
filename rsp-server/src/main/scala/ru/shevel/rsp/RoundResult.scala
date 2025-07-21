package ru.shevel.rsp

import io.circe.*

object RoundResult {
  given decoder: Decoder[RoundResult] = stringEnumDecoder[RoundResult]
  given encoder: Encoder[RoundResult] = stringEnumEncoder[RoundResult]
}

enum RoundResult:
  case Won
  case Lose
  case Tie
  case None