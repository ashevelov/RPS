package ru.shevel.rsp

case class OpponentResult(player_id: Long, player_name: String, card: Card, combo_factor:Int = 2, roundResult: RoundResult)