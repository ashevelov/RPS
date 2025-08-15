package ru.shevel.rsp

import cats.effect.IO

case class Contest(id:Long, opponents: List[Opponent], roundLog: List[RoundLog], winner:Long = 0){
  private val creationTime = System.currentTimeMillis()

  private def getAgeInMillis: Long = System.currentTimeMillis() - creationTime
  private def getAgeInSeconds: Double = getAgeInMillis / 1000.0

  //больше 10 мин небыло изменений
  def isAbandoned:Boolean ={
    getAgeInSeconds / 60 > 10
  }

  def isOld:Boolean = {
    getAgeInSeconds > 10
  }

  def updateCard(player_id:Long, card: Card):IO[Contest] = IO {
    val new_opponents = opponents.map { opponent =>
      if (opponent.player.id == player_id) {
        opponent.copy(select = card)
      } else {
        opponent
      }
    }
    this.copy(opponents = new_opponents)
  }

  def surrender(player_id: Long): IO[Contest] = {
    for{
      newOpponents <- IO{List(
        opponents.find(_.player.id == player_id).head.copy(hp = 0),
        opponents.find(_.player.id != player_id).head
        )}
      (updatedOpponents, winner) <- updateOpponents(newOpponents)
    } yield this.copy(opponents = updatedOpponents, winner = winner)
  }
  

  def bothTurnsDone(): IO[Boolean] = IO {
    opponents.forall(_.select != Card.None)
  }

  def isFilled: Boolean = opponents.size > 1

  def nextRound(): IO[Contest] = {
    for{
      (newRoundLog, newOpponents) <- determineRoundOutcome(opponents.head, opponents.last)
      _ <- train(newRoundLog)
      (updatedOpponents, winner) <- updateOpponents(newOpponents)

    } yield this.copy(opponents = updatedOpponents, roundLog = newRoundLog :: roundLog, winner = winner)
  }

  private def updateOpponents(opponents:List[Opponent]):IO[(List[Opponent], Long)] = {
    // Извлекаем проигравшего и победителя
    val (loser, winner) = opponents match {
      case loser :: winner :: Nil if loser.hp <= 0 && winner.hp > 0 => (loser, winner)
      case winner :: loser :: Nil if winner.hp > 0 && loser.hp <= 0 => (loser, winner)
      case _ => return IO((opponents, 0L))
    }

    // Обновляем статистику игроков
    val updatedLoserPlayer = loser.player.copy(lose = loser.player.lose + 1)
    val updatedWinnerPlayer = winner.player.copy(win = winner.player.win + 1)

    // Обновляем данные и возвращаем обновленных оппонентов
    for {
      _ <- Game.updatePlayer(updatedLoserPlayer)
      _ <- Game.updatePlayer(updatedWinnerPlayer)
    } yield (List(
      winner.copy(player = updatedWinnerPlayer),
      loser.copy(player = updatedLoserPlayer)
    ), updatedWinnerPlayer.id)
  }

  private def train(newRoundLog:RoundLog):IO[Unit] = {
    val prev_round_log = roundLog.sortBy(_.roundNumber).lastOption.getOrElse(RoundLog(roundNumber = 0, opponentResults = opponents.map{
      opp => OpponentResult(player_id = opp.player.id, player_name = opp.player.name, card = Card.None, combo_factor = 1, roundResult = RoundResult.None)
    }))
    val opp_1 = prev_round_log.opponentResults.head
    val opp_2 = prev_round_log.opponentResults.last
    if(opp_1.player_id != Bot.id) {
      Game.predictor.train(opp_1.card, opp_1.combo_factor, opp_2.card, opp_2.combo_factor, newRoundLog.opponentResults.find(_.player_id == opp_1.player_id).get.card)
    } else if (opp_2.player_id != Bot.id){
      Game.predictor.train(opp_2.card, opp_2.combo_factor, opp_1.card, opp_1.combo_factor, newRoundLog.opponentResults.find(_.player_id == opp_2.player_id).get.card)
    } else IO.unit
  }

  private def determineRoundOutcome(opponent1:Opponent, opponent2:Opponent):IO[(RoundLog, List[Opponent])] = IO{
    println("determineRoundOutcome")
    if (opponent1.select.tie(opponent2.select)) {
      (
        RoundLog(
          roundNumber = roundLog.size + 1,
          opponentResults = opponent1.tie :: opponent2.tie :: Nil
        ),
        List(
          opponent1.reset(0), 
          opponent2.reset(0)
        )
      )
    } else if (opponent1.select.beats(opponent2.select)) {
      (
        RoundLog(
          roundNumber = roundLog.size + 1,
          opponentResults = opponent1.winner :: opponent2.loser :: Nil
        ),
        List(
          opponent1.reset(0), 
          opponent2.reset(opponent1.damage())
        )
      )
    } else {
      (
        RoundLog(
          roundNumber = roundLog.size + 1,
          opponentResults = opponent1.loser :: opponent2.winner :: Nil
        ),
        List(
          opponent1.reset(opponent2.damage()),
          opponent2.reset(0)
        )
      )
    }
  }

  def addOpponent(opponent: Opponent): Contest =
    this.copy(opponents = opponents :+ opponent)

}
