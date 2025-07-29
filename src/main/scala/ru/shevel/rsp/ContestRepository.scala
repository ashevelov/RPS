package ru.shevel.rsp


trait ContestRepository[F[_]] {
  def getAll: F[List[Contest]]
  def create(opponent: Opponent): F[Contest]
  def delete(id: Long): F[Unit]
}