package software.amazon.smithy.openapi.fromsmithy

import java.util.Optional

package object protocols {

  implicit class OptionalOp[A](opt: Optional[A]) {
    def asScala: Option[A] = if (opt.isPresent()) Some(opt.get()) else None
  }

  implicit class OptionOps[A](opt: Option[A]) {
    def asJava: Optional[A] = opt match {
      case Some(value) => Optional.of(value)
      case None        => Optional.empty()
    }
  }
}
