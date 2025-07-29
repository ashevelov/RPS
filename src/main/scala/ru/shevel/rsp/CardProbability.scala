package ru.shevel.rsp

import cats.effect.IO

case class CardProbability(rock:Long = 1, scissors:Long = 1, paper:Long = 1, count:Long = 3) {
  def select(card: Card):IO[CardProbability] = IO{
    card match {
      case Card.None => this
      case Card.Rock => this.copy(rock = rock + 1, count = count + 1)
      case Card.Scissors => this.copy(scissors = scissors + 1, count = count + 1)
      case Card.Paper => this.copy(paper = paper + 1, count = count + 1)
    }
  }

  def predict(): IO[Predict] = IO{
    Predict(
      probRock = rock.toFloat / count,
      probScissors = scissors.toFloat / count,
      probPaper = paper.toFloat / count,
    )
  }
}