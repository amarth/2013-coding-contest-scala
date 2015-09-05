package ca.kijiji.contest

trait FineRecordParser {
  val streetTypes = Set("AVE", "CRT", "CIR", "TER", "WAY", "BLVD")
  def parseRecord(line: String): Option[(String, Int)]
}

object StreamFineRecordParser extends FineRecordParser{
  def parseRecord(line: String): Option[(String, Int)] = {

    try {
      val upUntilAddress = csvRevStream(line).drop(3)
      val addressParts = upUntilAddress.head
        .split(" ")
        .filterNot(p => {
        p.length < 2 || (p.charAt(0) <= '9' && p.charAt(0) >= '0')
      }).toList

      val street: String =
        if (addressParts == Nil)
          throw new IllegalArgumentException
        else if (addressParts.length == 1)
          addressParts.head
        else
          (addressParts.head :: addressParts.tail.filterNot(p =>
            p.length < 3 || streetTypes.contains(p))
            ).mkString(" ")

      val fine = upUntilAddress.drop(3).head.toInt

      Some((street, fine))

    } catch {
      case e:Exception => None
    }
  }

  def csvRevStream(csv:String):Stream[String] = {
    val lastIndex = csv.lastIndexOf(',')
    csv.substring(lastIndex+1) #:: csvRevStream(csv.substring(0,lastIndex))
  }
}
