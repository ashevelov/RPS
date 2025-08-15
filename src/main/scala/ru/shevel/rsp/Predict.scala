package ru.shevel.rsp

import cats.effect.IO
import ru.shevel.rsp

import scala.util.Random


case class Predict(probRock:Float, probScissors:Float, probPaper:Float) {
  // Проверяем корректность вероятностей
  require(probRock >= 0 && probRock <= 1, "Вероятность камня должна быть в диапазоне [0,1]")
  require(probScissors >= 0 && probScissors <= 1, "Вероятность ножниц должна быть в диапазоне [0,1]")
  require(probPaper >= 0 && probPaper <= 1, "Вероятность бумаги должна быть в диапазоне [0,1]")
  require(math.abs(probRock + probScissors + probPaper - 1) < 0.0001, "Сумма вероятностей должна равняться 1")

  def next(): IO[Card] = IO {
    // Генерируем случайное число от 0 до 1
    val randomValue = Random.nextFloat()

    println(s"probRock:$probRock, probScissors:$probScissors, probPaper:$probPaper, randomValue:$randomValue")
    // Определяем выбранную карту на основе накопленных вероятностей
    if (randomValue < probRock) {
      Card.Rock
    } else if (randomValue < probRock + probScissors) {
      Card.Scissors
    } else {
      Card.Paper
    }
  }

  def comboFactor(card:Card, factor:Int):IO[Predict] = IO{
    val factorProb = if(factor >= 10) 0.99f else factor / 10f
    card match {
      case rsp.Card.Rock => Predict(probRock * (1-factorProb) + factorProb, probScissors * (1-factorProb), probPaper * (1-factorProb))
      case rsp.Card.Paper => Predict(probRock * (1-factorProb), probScissors * (1-factorProb), probPaper * (1-factorProb) + factorProb)
      case rsp.Card.Scissors => Predict(probRock * (1-factorProb), probScissors * (1-factorProb) + factorProb, probPaper * (1-factorProb))
      case rsp.Card.None => this
    }
  }
}