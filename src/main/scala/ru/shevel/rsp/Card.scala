package ru.shevel.rsp

import io.circe.*

import scala.util.Random

object Card {
  given decoder: Decoder[Card] = stringEnumDecoder[Card]
  given encoder: Encoder[Card] = stringEnumEncoder[Card]
}

enum Card:
  case Rock
  case Paper
  case Scissors
  case None

  private val cards = List(Card.Rock, Card.Paper, Card.Scissors)
  
  def stronger: Card = this match {
    case Card.Rock => Card.Paper
    case Card.Paper => Card.Scissors
    case Card.Scissors => Card.Rock
    case Card.None => Random.shuffle(cards).head
  }
  
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