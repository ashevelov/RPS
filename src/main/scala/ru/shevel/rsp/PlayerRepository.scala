package ru.shevel.rsp

import java.time.LocalDateTime

// Интерфейс репозитория
trait PlayerRepository[F[_]] {
  def getAll: F[List[Player]]
  def getById(id: Long): F[Player]
  def create(player: Player): F[Player]
  def update(player: Player): F[Unit]
  def delete(id: Long): F[Unit]
}