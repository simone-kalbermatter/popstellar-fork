package ch.epfl.pop.model.network.method

import ch.epfl.pop.model.network.method.message.Message

trait ParamsWithMessage extends Params {
  val message: Message
}
