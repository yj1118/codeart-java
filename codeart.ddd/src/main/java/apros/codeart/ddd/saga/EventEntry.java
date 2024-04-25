package apros.codeart.ddd.saga;

import apros.codeart.dto.DTObject;

/**
 * {@name 条目名称，也就是对应的事件名称}
 */
record EventEntry(String name, DTObject arg) {

}
