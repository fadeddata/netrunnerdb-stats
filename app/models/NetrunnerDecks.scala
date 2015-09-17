package models

import play.api.libs.json._
import play.api.Play.current

object NetrunnerDecks {
  def path = play.api.Play.classloader.getResource("public/decks.json").getPath
  def decks = Json.parse(scala.io.Source.fromFile(path).mkString)
}