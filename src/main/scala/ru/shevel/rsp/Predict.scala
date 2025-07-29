package ru.shevel.rsp

import cats.effect.IO

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
}