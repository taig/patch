package io.taig.patch

final case class Naming(f: String => String) extends AnyVal {
  def apply(value: String): String = f(value)
}

object Naming {
  val camelCase: Naming = Naming { value =>
    val index = value.lastIndexOf('$') match {
      case -1 =>
        value.lastIndexOf('.') match {
          case -1    => None
          case index => Some(index)
        }
      case index => Some(index)
    }

    val name = index.map(index => value.substring(index + 1)).getOrElse(value)
    name.updated(0, name(0).toLower)
  }
}
