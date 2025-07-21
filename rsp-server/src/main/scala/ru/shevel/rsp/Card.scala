package ru.shevel.rsp

import io.circe.*

object Card {
  given decoder: Decoder[Card] = stringEnumDecoder[Card]
  given encoder: Encoder[Card] = stringEnumEncoder[Card]
}

enum Card:
  case Rock
  case Paper
  case Scissors
  case None

  def beats(other: Card): Boolean =
    (this, other) match {
      case (Rock, Scissors) => true
      case (Paper, Rock) => true
      case (Scissors, Paper) => true
      case _ => false
    }

  def tie(other: Card): Boolean =
    this == other

  def losesTo(other: Card): Boolean =
    !beats(other) && !tie(other)