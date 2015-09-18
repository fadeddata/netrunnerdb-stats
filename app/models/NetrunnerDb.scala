package models

import java.io.File
import play.api.libs.ws._
import play.api.Play.current
import play.api.libs.json._
import org.mapdb._
import scala.collection.JavaConversions._
import scala.concurrent._
import javax.inject._
import play.api.inject.ApplicationLifecycle

@Singleton
class NetrunnerDbCache @Inject() (lifecycle: ApplicationLifecycle) {
  val dbFile = new File("database.db")
  val db = DBMaker.newFileDB(dbFile).make()
  val cache = db.getHashMap[String, String]("cache")

  def urls(implicit ec: ExecutionContext) = Future { cache.keys }

  def getUrl(url: String)(implicit ec: ExecutionContext) = {
    Future { cache.toMap.get(url) } flatMap {
      case Some(data) =>
        Future.successful(Json.parse(data))
      case None =>
        println(s"requesting data for: $url")
        WS.url(url).get.map { data =>
          val json = data.json
          if ((json \ "error").toOption.isEmpty) {
            cache.put(url, Json.stringify(json))
          }
          json
        }
    }
  }

  lifecycle.addStopHook { () =>
    Future.successful {
      db.commit()
      db.close()
    }
  }
}

class NetrunnerDb @Inject()(cache: NetrunnerDbCache) {

  val deckUrlsJson = """
[
["http://netrunnerdb.com/en/decklist/24158/good-things-on-sale-stranger-1st-place-plano-tx-regionals", "http://netrunnerdb.com/en/decklist/14677/can-o-whupass", "http://netrunnerdb.com/en/decklist/25678/injection-polish-nationals-winner", "http://netrunnerdb.com/en/decklist/21538/l4j-looking-for-job-madison-wi-regional-2nd-place", "http://netrunnerdb.com/en/decklist/23615/faust-joust-v1-0-1st-place-berkeley-anrpc-qualifier", "http://netrunnerdb.com/en/decklist/25336/whizz-fizz-australian-nationals-2015-winner", "http://netrunnerdb.com/en/decklist/24684/grindstone-aka-anti-netrunner-", "http://netrunnerdb.com/en/decklist/19404/paige-me-red-and-call-me-valencia-london-regional-2nd", "http://netrunnerdb.com/en/decklist/24190/valtman-socal-regional-1st-place-", "http://netrunnerdb.com/en/decklist/25994/reina-gang-member-manchester-babw-1st-place", "http://netrunnerdb.com/en/decklist/17417/soul-siphon-1st-place-undefeated-uncle-s-games-bellevue", "http://netrunnerdb.com/en/decklist/25672/reina-gang-member-polish-nationals-top16", "http://netrunnerdb.com/en/decklist/18233/qimpossible-1st-place-petrie-s-family-games-sc", "http://netrunnerdb.com/en/decklist/26080/as-the-crow-flies", "http://netrunnerdb.com/en/decklist/26207/que3tzal-faust", "http://netrunnerdb.com/en/decklist/14503/slysquids-mother-f-deck-store-championship-winner-", "http://netrunnerdb.com/en/decklist/14180/i-prepaid-for-this-motherf-ing-event", "http://netrunnerdb.com/en/decklist/19047/starkandtully-s-combo-maxx-4th-in-stimhack-invitational-", "http://netrunnerdb.com/en/decklist/23870/durham-nc-atomic-empire-regionals-2015-8th-place-runner", "http://netrunnerdb.com/en/decklist/25036/career-kim-top-16-australian-nationals-5-1-swiss-rounds", "http://netrunnerdb.com/en/decklist/25379/-nerdrunners-fist-of-the-morning-star-v1-0"],
["http://netrunnerdb.com/en/decklist/25087/-unbroken-no-icebreakers", "http://netrunnerdb.com/en/decklist/23200/gang-sign-leela-the-turntable-version-aka-turntableela-", "http://netrunnerdb.com/en/decklist/20850/leela-endless-waltz-v5-sheffield-regional-winner-", "http://netrunnerdb.com/en/decklist/26062/iain-s-creepy-sleepover", "http://netrunnerdb.com/en/decklist/23891/1st-place-spring-tournament-teg-colorado", "http://netrunnerdb.com/en/decklist/22534/it-was-me-austin-it-was-me-all-along-v0-8", "http://netrunnerdb.com/en/decklist/23903/are-you-watching-closely-2nd-at-anrpc-wausau-jul-18", "http://netrunnerdb.com/en/decklist/23258/geist-you-re-are-doing-it-wrong-", "http://netrunnerdb.com/en/decklist/24777/geist-gives-it-two-thumbs-up", "http://netrunnerdb.com/en/decklist/25821/-but-he-was-still-hungry-", "http://netrunnerdb.com/en/decklist/11014/the-solution-worlds-3rd-place-runner-deck-", "http://netrunnerdb.com/en/decklist/11990/switchblade-gabe-401-games-winning-runner-list-dec-7-14-", "http://netrunnerdb.com/en/decklist/23288/kelf-s-swift-shadow-lock-", "http://netrunnerdb.com/en/decklist/5431/modified-notorious-daily-quester-chicago-regionals-2014-", "http://netrunnerdb.com/en/decklist/11285/no-time-to-waste-v1-3-1st-place-22-players-5-rounds-", "http://netrunnerdb.com/en/decklist/26133/club-mill-featuring-dj-fisk", "http://netrunnerdb.com/en/decklist/25694/mergers-and-acquisitions", "http://netrunnerdb.com/en/decklist/24554/fisk-in-a-red-dress", "http://netrunnerdb.com/en/decklist/25979/wittertainment-irish-nationals-1st-place", "http://netrunnerdb.com/en/decklist/25808/-anarch-breakers-et-cetera-5th-6th-place-us-nationals-", "http://netrunnerdb.com/en/decklist/14970/and4omeda", "http://netrunnerdb.com/en/decklist/25025/-eventually-we-will-win-german-nationals-3rd-place", "http://netrunnerdb.com/en/decklist/24350/money-for-running-and-clicks-for-free-v3-4th-babw-leeds-", "http://netrunnerdb.com/en/decklist/16187/the-crimson-leaves-strike-2015-store-championship-winner"],
["http://netrunnerdb.com/en/decklist/25699/it-s-a-gameday-for-bagbiter-2nd-place-polish-nationals", "http://netrunnerdb.com/en/decklist/16557/goodbye-to-you-card-kingdom-store-championship-1st-place", "http://netrunnerdb.com/en/decklist/23912/faust-theory-10th-verona-regional", "http://netrunnerdb.com/en/decklist/23925/congress", "http://netrunnerdb.com/en/decklist/24504/typical-ppvp-kate-1st-place-at-2015-national-", "http://netrunnerdb.com/en/decklist/17510/demon-kate-2-0-1st-place-mtg-deals-sc-san-gabriel-ca-", "http://netrunnerdb.com/en/decklist/21626/scholar-of-stealth-1st-place-uroboros-cup-2015-67-players-", "http://netrunnerdb.com/en/decklist/24339/barely-legal-top-16-us-nationals-", "http://netrunnerdb.com/en/decklist/25827/fifty-five-km-s", "http://netrunnerdb.com/en/decklist/16055/dumpster-gamble-1st-place-greenlake-games-store-champion", "http://netrunnerdb.com/en/decklist/25647/deep-green-street-chess-", "http://netrunnerdb.com/en/decklist/23667/dirty-hands-4-0", "http://netrunnerdb.com/en/decklist/21621/seamus-stealth-kit-4th-place-uk-nationals-", "http://netrunnerdb.com/en/decklist/15265/red-dress-kit-2nd-place-unique-gifts-games-store-champio", "http://netrunnerdb.com/en/decklist/9665/hard-stealth-kit", "http://netrunnerdb.com/en/decklist/10162/no-sleep-natman-atman-nasir-primer-guide-", "http://netrunnerdb.com/en/decklist/23372/nasir-s-deal-with-the-devil", "http://netrunnerdb.com/en/decklist/18891/two-knives-and-a-paintbrush", "http://netrunnerdb.com/en/decklist/21217/everything-2nd-place-professor-at-euregio-43-players-", "http://netrunnerdb.com/en/decklist/25947/the-professor-redux", "http://netrunnerdb.com/en/decklist/24905/you-got-an-f-see-me-after-class-"],
["http://netrunnerdb.com/en/decklist/20145/waldemar-hb-2x-top-8-regionals-dmqt-winner-", "http://netrunnerdb.com/en/decklist/22917/polish-hb-1st-undefeated-wroc-aw-regionals-", "http://netrunnerdb.com/en/decklist/25094/hb-webz-nordic-national-championship-1st-place-", "http://netrunnerdb.com/en/decklist/25144/spooky-cybernetics-top-cybernetics-division-us-nationals-", "http://netrunnerdb.com/en/decklist/21432/the-spiteful-mind", "http://netrunnerdb.com/en/decklist/23408/uncanny-valley-v0-1", "http://netrunnerdb.com/en/decklist/19803/n3x7-ru5h-d3c", "http://netrunnerdb.com/en/decklist/16145/transformer-2nd-48-mead-hall-mn-sc-undefeated-", "http://netrunnerdb.com/en/decklist/20340/next-king-of-england-v6", "http://netrunnerdb.com/en/decklist/18235/-roid-rage-1st-place-petrie-s-family-games-sc", "http://netrunnerdb.com/en/decklist/23586/1-21-gigawatts-great-scott-", "http://netrunnerdb.com/en/decklist/10453/the-tower-of-babel", "http://netrunnerdb.com/en/decklist/25697/last-round-win-polish-nationals-2nd-place", "http://netrunnerdb.com/en/decklist/4745/scorched-imaging", "http://netrunnerdb.com/en/decklist/24601/7-point-ci", "http://netrunnerdb.com/en/decklist/12391/why-are-you-doing-that-v3", "http://netrunnerdb.com/en/decklist/25926/shoot-the-jank", "http://netrunnerdb.com/en/decklist/24991/id-crisis-1st-place-cb-blue-sun-dammit-janus-", "http://netrunnerdb.com/en/decklist/21688/bioroid-kill-squad-1st-may-gnk-dragon-s-lair-austin-tx", "http://netrunnerdb.com/en/decklist/11316/sansan-south-vince-s-standard-stronger-together-deck", "http://netrunnerdb.com/en/decklist/19011/chain-of-strength"],
["http://netrunnerdb.com/en/decklist/26049/barrier-bay", "http://netrunnerdb.com/en/decklist/24544/the-cartographer-s-dilemma-v1-0", "http://netrunnerdb.com/en/decklist/26052/the-bug-zapper", "http://netrunnerdb.com/en/decklist/25173/who-needs-a-butcher-australian-nationals-2015-winner", "http://netrunnerdb.com/en/decklist/26158/jinteki-pe-mindthorns-2-0-", "http://netrunnerdb.com/en/decklist/22727/bag-of-tricks-2nd-oslo-regionals-undefeated-8-0-top-seed", "http://netrunnerdb.com/en/decklist/19187/six-point-exploding-heart-technique", "http://netrunnerdb.com/en/decklist/19215/f-is-for-flip", "http://netrunnerdb.com/en/decklist/23884/fugu-v1-3-2015-north-carolina-regionals-undefeated-", "http://netrunnerdb.com/en/decklist/25674/lunar-kudzu-v2-1", "http://netrunnerdb.com/en/decklist/17830/bloodlock-an-unexpected-monster-v0-99", "http://netrunnerdb.com/en/decklist/23905/passchendaele-2nd-place-newark-de-anrpc-", "http://netrunnerdb.com/en/decklist/25014/tower-of-pain", "http://netrunnerdb.com/en/decklist/10690/institute-of-tennis-10th-place-at-worlds-", "http://netrunnerdb.com/en/decklist/16578/no-siphons-please-card-kingdom-store-championship-1st-plac", "http://netrunnerdb.com/en/decklist/25291/harmony-medtech-saves-the-world", "http://netrunnerdb.com/en/decklist/5136/the-thing-that-should-not-be", "http://netrunnerdb.com/en/decklist/12035/speedy-snare-delivery-service-", "http://netrunnerdb.com/en/decklist/25085/replicating-perfection-1st-place-at-german-nationals-", "http://netrunnerdb.com/en/decklist/26266/bizzare-rp-rush-deck-aka-tampa-rush", "http://netrunnerdb.com/en/decklist/3946/replicating-kills-v1-0-a-guide-how-to-play-rp-properly-", "http://netrunnerdb.com/en/decklist/23286/kelf-s-little-garden-", "http://netrunnerdb.com/en/decklist/23531/code-gate-into-the-mind-v1-1", "http://netrunnerdb.com/en/decklist/21693/psisquid-v2-0"],
["http://netrunnerdb.com/en/decklist/25391/haarpsichord-doomsday-2-3-public-sympathy-shield-1st-place-", "http://netrunnerdb.com/en/decklist/25536/the-defrosted-honeypot", "http://netrunnerdb.com/en/decklist/24576/kitsunichord-or-take-the-bait", "http://netrunnerdb.com/en/decklist/26002/choose-wisely-the-holy-grail", "http://netrunnerdb.com/en/decklist/16988/butcher-shop-v4-1st-place-uncle-s-games-sc-redmond-wa-", "http://netrunnerdb.com/en/decklist/26074/from-mirrormorph-to-sansan", "http://netrunnerdb.com/en/decklist/22037/when-the-flash-wears-yellow-v-2-undefeated-bratislava-regi", "http://netrunnerdb.com/en/decklist/11578/nbn-grail-midway", "http://netrunnerdb.com/en/decklist/2957/twiy-astrobiotics", "http://netrunnerdb.com/en/decklist/12733/moonbase-alpha", "http://netrunnerdb.com/en/decklist/25981/restructured-databull-irish-national-1st-place", "http://netrunnerdb.com/en/decklist/22568/nbn-engineering-the-future-10-wins-0-losses"],
["http://netrunnerdb.com/en/decklist/25677/deus-ex-polish-nationals-winner", "http://netrunnerdb.com/en/decklist/24367/rangus-security-12-4-nat-l-anrpc-regionals-", "http://netrunnerdb.com/en/decklist/26251/watch-em-all", "http://netrunnerdb.com/en/decklist/23599/the-black-knight-babw-york-winner", "http://netrunnerdb.com/en/decklist/24256/lanri-2-0-socal-regional-1st-place-", "http://netrunnerdb.com/en/decklist/15621/the-government-takeover-leisure-games-london-2nd-place", "http://netrunnerdb.com/en/decklist/24824/fascism-three-ways-to-win", "http://netrunnerdb.com/en/decklist/25113/titan-1st-place-d-c-anrpc-mac-qualifier-", "http://netrunnerdb.com/en/decklist/25862/vanity-and-greed", "http://netrunnerdb.com/en/decklist/25623/donald-trump-for-president", "http://netrunnerdb.com/en/decklist/19483/quinns-tier-1-2-gagarin-deck-6th-at-london-regionals-", "http://netrunnerdb.com/en/decklist/26173/expo-in-space", "http://netrunnerdb.com/en/decklist/24580/hypermodernism-3-1-you-re-going-on-the-list-", "http://netrunnerdb.com/en/decklist/16031/49-shades-of-grey", "http://netrunnerdb.com/en/decklist/8936/uncorrodable", "http://netrunnerdb.com/en/decklist/24162/-twa-oaktown-funk-you-up-3-0-", "http://netrunnerdb.com/en/decklist/24353/wu-tang-financial-v2-6-1-in-swiss-at-us-nat-l-", "http://netrunnerdb.com/en/decklist/22597/-twa-oaktown-funk-you-up-", "http://netrunnerdb.com/en/decklist/24275/i-hate-you-and-i-want-you-to-know-it", "http://netrunnerdb.com/en/decklist/16734/constructing-cyberspace-version-1-0", "http://netrunnerdb.com/en/decklist/20249/great-wall-of-weyland-stargazer-edition"]
]
"""
  val deckIds = Json.parse(deckUrlsJson).as[Seq[Seq[String]]].flatten.map(_.split('/')(5).toLong)

  def getDecks(implicit ec: ExecutionContext) = cache.urls.flatMap { urls =>
    Future.sequence(
      urls.filter(_.split('/').lift(4).contains("decklist")).map { url =>
        cache.getUrl(url)
      }
    )
  }

  def getDeck(id: Long)(implicit ec: ExecutionContext) = {
    val url = s"http://netrunnerdb.com/api/decklist/$id"
    cache.getUrl(url)
  }

  def getCard(code: String)(implicit ec: ExecutionContext) = {
    val url = s"http://netrunnerdb.com/api/card/$code"
    cache.getUrl(url)
  }

  def getDecks(deckIds: Seq[Long])(implicit ec: ExecutionContext) = {
    Future.sequence(
      deckIds map { deckId =>
        getDeck(deckId)
      } 
    ).map { decks =>
      JsArray(decks)
    }
  }

  def getCardCodes(decks: JsValue) = 
    (decks \\ "cards").flatMap(_.asOpt[Map[String, Long]])

  def getCardCodeCounts(codes: Seq[Map[String, Long]]) = 
    codes.flatMap(_.keys).groupBy(identity).map(c => c._1 -> c._2.size)

  def getCards(counts: Map[String, Int])(implicit ec: ExecutionContext) =
    Future.sequence(
      counts.map { case (code, count) =>
        getCard(code).map(card => card -> count)
      }
    )

  def groupBySetNameAndSum(cards: Seq[(JsValue, Int)]) = 
    cards.map { card =>
      (card._1(0) \ "setname").asOpt[String] -> card._2
    }.groupBy(_._1).map { case (set, counts) =>
      set.getOrElse("") -> counts.map(_._2).sum
    }.toList.sortBy(_._2).reverse

  def getDistance(deck1: JsValue, deck2: JsValue) = {
    val deck1CardCodes = (deck1 \ "cards").asOpt[Map[String, Long]].getOrElse(Map.empty[String, Long]).keys.toSeq.sorted
    val deck2CardCodes = (deck2 \ "cards").asOpt[Map[String, Long]].getOrElse(Map.empty[String, Long]).keys.toSeq.sorted

    EditDistance.editDist(deck1CardCodes, deck2CardCodes)
  }
}