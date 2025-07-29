package ru.shevel.rsp

import cats.effect.{IO, IOApp}

import scala.concurrent.duration.*

object RSPServer extends IOApp.Simple {
  val run: IO[Unit] = {
    for {
      _ <- Game.run.start
      _ <- Http.run.start
      _ <- IO.never // this is needed so that the server keeps running
    } yield ()
  }
}