package app

import api.UrlValue

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.element

import scala.language.postfixOps

object Parser {
    def pageTitle(url: UrlValue): String = {
        val browser: Browser = JsoupBrowser()
        val doc: Browser#DocumentType = browser.get(url.str)

        doc >> element("html") >> element("head") >> element("title") >> allText
    }
}
