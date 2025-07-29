package ru.shevel.rsp

import cats.effect.*
import cats.syntax.all.*

import scala.util.Random

class InMemoryUserRepository extends PlayerRepository[IO] {
  private val baseNames = Seq("Властелин", "Overlord", "Архангел", "DethBringer", "Император", "KnightX", "Завоеватель", "Titan",
    "Патриарх", "Storm", "Безликий", "Phantom", "Сириус", "Nexus", "Хаос", "Blaze", "Вулкан", "Seraph", "Легион", "Viper",
    "Гранит", "Apex", "Оракул", "Zenith", "Вершитель", "Shadow", "Демон", "Specter", "Небесный", "Inferno", "Страж", "Slayer",
    "Ястреб", "Venom", "Гром", "Blade", "Хранитель", "Raptor", "Искра", "Reaper", "Феникс", "Drakon", "Магистр", "Titania",
    "Лидер", "Warlord", "Энигма", "Vortex", "Король", "Zenith")
  private var players: List[Player] = List.empty
  private var nextId: Long = 0

  override def getAll: IO[List[Player]] =
    IO(players)

  override def getById(id: Long): IO[Player] =
    players.find(_.id == id) match
      case Some(player) => IO(player)
      case None =>
        val rndName = baseNames(Random.nextInt(baseNames.size))
        create(Player(id, rndName, 0, 0))

  override def create(player: Player): IO[Player] =
    for {
      now <- IO.realTime
//      newPlayer = player.copy(id = id)
      _ <- IO {
//        nextId += 1
        players = player :: players
      }
    } yield player

  override def update(player: Player): IO[Unit] =
    IO {
      println("update")
      players = players.map { p =>
        if (p.id == player.id) player else p
      }
      println("players: " + players.mkString("\n"))
    }

  override def delete(id: Long): IO[Unit] =
    IO {
      players = players.filterNot(_.id == id)
    }
}