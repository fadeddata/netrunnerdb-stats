package controllers

import play.api._, libs.json._, mvc._
import models.{EditDistance, NetrunnerDb}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import javax.inject._

class Application @Inject()(netrunnerDb: NetrunnerDb) extends Controller {

  def popularSets = Action.async {
    for {
      decks <- netrunnerDb.getDecks(netrunnerDb.deckIds)
      cardCodes = netrunnerDb.getCardCodes(decks)
      cardCodeCounts = netrunnerDb.getCardCodeCounts(cardCodes)
      cards <- netrunnerDb.getCards(cardCodeCounts)
      setNameAndSum = netrunnerDb.groupBySetNameAndSum(cards.toSeq)
    } yield {
      val data = setNameAndSum.map { case (set, count) =>
        JsObject(Seq(
          "set"   -> JsString(set),
          "count" -> JsNumber(count)
        ))
      }
      Ok(Json.toJson(data))
    }
  }

  def popularCards = Action.async {
    for {
      decks <- netrunnerDb.getDecks(netrunnerDb.deckIds)
      cardCodes = netrunnerDb.getCardCodes(decks)
      cardCodeCounts = netrunnerDb.getCardCodeCounts(cardCodes)
      cards <- netrunnerDb.getCards(cardCodeCounts)
    } yield {
      val ret = cards.toSeq.sortBy(_._2).map { case (json, count) =>
        JsObject(Seq(
          "title"    -> (json(0) \ "title").getOrElse(JsNull),
          "set_name" -> (json(0) \ "setname").getOrElse(JsNull),
          "side"     -> (json(0) \ "side").getOrElse(JsNull),
          "count"    -> JsNumber(count)
        ))
      }.reverse

      Ok(Json.toJson(ret))
    }
  }

  def deckDistance(deck1Id: Long, deck2Id: Long) = Action.async {
    for {
      deck1 <- netrunnerDb.getDeck(deck1Id)
      deck2 <- netrunnerDb.getDeck(deck2Id)
    } yield {
      val deck1CardCodes = (deck1 \ "cards").asOpt[Map[String, Long]].getOrElse(Map.empty[String, Long]).keys.toSeq.sorted
      val deck2CardCodes = (deck2 \ "cards").asOpt[Map[String, Long]].getOrElse(Map.empty[String, Long]).keys.toSeq.sorted

      Ok(EditDistance.editDist(deck1CardCodes, deck2CardCodes).toString)
    }
  }
}