package app

trait ConsoleLogger {
  def log(msg: String): Unit = println(msg)
}
