package ru.shevel.rsp

import cats.Functor
import cats.effect.kernel.Sync
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

class RPSPredictor(using Sync[IO]) {
    private lazy val modelRef: Ref[IO, Map[(Card, Int, Card, Int), CardProbability]] =
        Ref.of(Map.empty[(Card, Int, Card, Int), CardProbability]).unsafeRunSync()

    // Обучение модели
    def train(pPrevCard: Card, pFactor: Int, oPrevCard: Card, oFactor: Int, selectedCard: Card): IO[Unit] = {
        val key = (pPrevCard, if(pFactor > 5) 5 else pFactor, oPrevCard, if(oFactor > 5) 5 else oFactor)
        println(s"train: key $key , card $selectedCard" )
        for {
            model <- modelRef.get
            updatedRate = model.get(key) match {
                case Some(rate) => rate.select(selectedCard)
                case None => CardProbability().select(selectedCard)
            }
            _ <- updatedRate.flatMap { newRate =>
                modelRef.set(model + (key -> newRate))
            }
        } yield ()
    }

    // Сохранение модели в файл
    def saveToFile(path: String): IO[Unit] = {
        for {
            model <- modelRef.get
            file <- IO(new FileOutputStream(path))
            _ <- IO {
                val out = new ObjectOutputStream(file)
                out.writeObject(model)
                out.close()
            }
        } yield ()
    }

    // Загрузка модели из файла
    def loadFromFile(path: String): IO[Unit] = {
        for {
            file <- IO(new FileInputStream(path))
            model <- IO {
                val in = new ObjectInputStream(file)
                val loadedModel = in.readObject().asInstanceOf[Map[(Card, Int, Card, Int), CardProbability]]
                in.close()
                loadedModel
            }
            _ <- modelRef.set(model)
        } yield ()
    }

    // Предсказание следующего хода
    def predict(pPrevCard: Card, pFactor: Int, oPrevCard: Card, oFactor: Int): IO[Card] = {
        val key = (pPrevCard, if(pFactor > 5) 5 else pFactor, oPrevCard, if(oFactor > 5) 5 else oFactor)

        for {
            model <- modelRef.get
            rate = model.getOrElse(key, CardProbability())
            predict <- rate.predict()
            predictByFactor <- predict.comboFactor(oPrevCard, oFactor)
            card <- predictByFactor.next()
        } yield card
    }
}
