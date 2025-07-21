package ru.shevel.rsp



case class Opponent(player: Player, hp: Int = 15, select:Card = Card.None, combo_card: Card = Card.None, combo_factor:Int = 1){
  def damage(): Int = if(select == combo_card) combo_factor else 1
  def reset(damage:Int): Opponent ={
    println(s"player: ${player.name} select:$select combo_card:$combo_card")
    this.copy(hp = hp - damage, select = Card.None, combo_card = select, combo_factor = if(select == combo_card) combo_factor + 1 else 1)
  }
  def winner:OpponentResult = OpponentResult(player_id = player.id, player_name = player.name, card = select, combo_factor = damage(), roundResult = RoundResult.Won)
  def loser:OpponentResult = OpponentResult(player_id = player.id, player_name = player.name, card = select, combo_factor = damage(), roundResult = RoundResult.Lose)
  def tie:OpponentResult = OpponentResult(player_id = player.id, player_name = player.name, card = select, combo_factor = damage(), roundResult = RoundResult.Tie)
}