fun box(): String {
  val l: Long = 1
  val l2: Long = 2
  val r = l.rangeTo(l2)
  return if (r.start == l && r.end == l2) "OK" else "fail"
}