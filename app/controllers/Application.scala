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

  def deckDistance(deck1Id: Long) = Action.async {
    for {
      deck1 <- netrunnerDb.getDeck(deck1Id)
      decks <- netrunnerDb.getDecks
    } yield {
      Ok(JsArray(
        decks.map { deck =>
          netrunnerDb.getDistance(deck1, deck) -> deck
        }.toSeq.sortBy(_._1).map { case (score, deck) =>
          JsObject(Seq(
            "score" -> JsNumber(score),
            "deck" -> deck
          ))
        }
      ))
    }
  }

  def decks = Action.async {
    for {
      decks <- netrunnerDb.getDecks
    } yield {
      Ok(JsArray(decks.toSeq))
    }
  }
}