package s2js.adapters.js.dom

abstract class CanvasPixelArray {
    val length: Long = 0L

    //TODO: octet       retval
    def getter(index: Long): Long {}

    //TODO: octet value
    def setter(index: Long, value: Long) {}
}