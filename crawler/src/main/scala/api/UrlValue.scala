package api

case class UrlValue(str: String) {
  lazy val hash: Int = util.hashing.MurmurHash3.stringHash(str)
}
