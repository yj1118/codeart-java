package com.apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.apros.codeart.ddd.dynamic.ListEntry;
import com.apros.codeart.ddd.dynamic.ObjectEntry;
import com.apros.codeart.ddd.dynamic.TypeEntry;
import com.apros.codeart.ddd.dynamic.ValueEntry;
import com.apros.codeart.runtime.TypeUtil;
import com.google.common.collect.Iterables;

class DTOTypeMetadataTest {

	@Test
	public void ParseType1() {
		final String code = "{id:'int',name:'ascii,10'}";
		var metadata = DTObject.getMetadata(code);

		var es = metadata.getEntries();

		AssertValue(es.get(0), "id", "int");
		AssertValue(es.get(1), "name", "ascii", "10");
	}

	@Test
	public void parseType2() {
		final String code = "{id:'int',name:'ascii,10',person:{name:'string,10',sex:'byte'}}";
		var metadata = DTObject.getMetadata(code);

		var es = metadata.getEntries();

		AssertValue(es.get(0), "id", "int");
		AssertValue(es.get(1), "name", "ascii", "10");
		AssertObject(es.get(2), "person", "person");
	}

	@Test
	public void ParseType3() {
		String code = "{id:'int',name:'ascii,10',person:{name:'string,10',sex:'byte'},first:'person',menu:{person:{id:'long',name:'string,5'},name:'string,10',parent:'menu',childs:['menu']},others:[{id:'int',name:'string',person:'menu.person'}],values:['string,10'],others2:'others'}";
		var metadata = DTObject.getMetadata(code);

		var es = metadata.getEntries();

		AssertValue(es.get(0), "id", "int");
		AssertValue(es.get(1), "name", "ascii", "10");

		AssertObject(es.get(2), "person", "person");
		var person = TypeUtil.as(es.get(2), ObjectEntry.class);
		var personChilds = person.getChilds();
		AssertValue(personChilds.get(0), "name", "string", "10");
		AssertValue(personChilds.get(1), "sex", "byte");

		AssertObject(es.get(3), "first", "person");
		var first = TypeUtil.as(es.get(3), ObjectEntry.class);
		var firstChilds = first.getChilds();
		AssertValue(firstChilds.get(0), "name", "string", "10");
		AssertValue(firstChilds.get(1), "sex", "byte");

		AssertMenu(es.get(4));

		AssertOthers(es.get(5), "others", "others");

		AssertList(es.get(6), "values", "values");
		var valuesItem = TypeUtil.as(es.get(6), ListEntry.class).getItemEntry();
		AssertValue(valuesItem, "values.item", "string", "10");

		AssertOthers(es.get(7), "others2", "others");

	}

	@Test
	public void ParseType4() {
		// const string code =
		// "{id:'int',name:'ascii,10',person:{name:'string,10',sex:'byte'},first:'person',menu:{person:{id,nam}},name:'string,10',parent:'menu',childs:['menu']},others:[{id:'int',name:'string',person:'menu.person'}]}";
		String code = "user:{id:'int',name:'ascii,10',son:'user'}";
		var metadata = DTObject.getMetadata(code);

		var es = metadata.getEntries();

		AssertValue(es.get(0), "id", "int");
		AssertValue(es.get(1), "name", "ascii", "10");

		AssertObject(es.get(2), "son", "user");
		var sonES = TypeUtil.as(es.get(2), ObjectEntry.class).getChilds();

		AssertValue(sonES.get(0), "id", "int");
		AssertValue(sonES.get(1), "name", "ascii", "10");
		AssertObject(sonES.get(2), "son", "user");
	}

	private void AssertOthers(TypeEntry entry, String name, String typeName) {
		AssertList(entry, name, typeName);
		var othersItemEntry = TypeUtil.as(entry, ListEntry.class).getItemEntry();
		AssertObject(othersItemEntry, "others.item", "others.item");
		var obj = TypeUtil.as(othersItemEntry, ObjectEntry.class);
		var childs = obj.getChilds();

		AssertValue(childs.get(0), "id", "int");
		AssertValue(childs.get(1), "name", "string");
		AssertMenuPerson(childs.get(2));
	}

	private void AssertMenu(TypeEntry entry) {
		AssertObject(entry, "menu", "menu");
		AssertMenuBase(entry);

		var menu = TypeUtil.as(entry, ObjectEntry.class);
		var menuChilds = menu.getChilds();

		// 验证数组
		AssertList(menuChilds.get(3), "childs", "menu.childs");
		var menuChildsEntry = TypeUtil.as(menuChilds.get(3), ListEntry.class);
		AssertObject(menuChildsEntry.getItemEntry(), "menu.childs.item", "menu");
		AssertMenuBase(menuChildsEntry.getItemEntry());
	}

	private void AssertMenuBase(TypeEntry entry) {
		var menu = TypeUtil.as(entry, ObjectEntry.class);
		var menuChilds = menu.getChilds();

		AssertMenuPerson(menuChilds.get(0));
		AssertValue(menuChilds.get(1), "name", "string", "10");

		AssertObject(menuChilds.get(2), "parent", "menu");
		var menuParent = TypeUtil.as(menuChilds.get(2), ObjectEntry.class);
		var menuParentChilds = menuParent.getChilds();
		AssertMenuPerson(menuParentChilds.get(0));
		AssertValue(menuParentChilds.get(1), "name", "string", "10");
	}

	private void AssertMenuPerson(TypeEntry entry) {
		AssertObject(entry, "person", "menu.person");
		var menuPerson = TypeUtil.as(entry, ObjectEntry.class);
		var menuPersonChilds = menuPerson.getChilds();
		AssertValue(menuPersonChilds.get(0), "id", "long");
		AssertValue(menuPersonChilds.get(1), "name", "string", "5");
	}

	private void AssertValue(TypeEntry entry, String name, String typeName, String... descriptions) {
		var value = TypeUtil.as(entry, ValueEntry.class);
		assertNotNull(value);

		assertEquals(value.getName(), name);
		assertEquals(value.getTypeName(), typeName);

		var actualDescriptions = value.getDescriptions();
		var size = Iterables.size(actualDescriptions);
		assertEquals(descriptions.length, size);

		for (var i = 0; i < size; i++) {
			assertEquals(descriptions[i], Iterables.get(actualDescriptions, i));
		}
	}

	private void AssertObject(TypeEntry entry, String name, String typeName) {
		var obj = TypeUtil.as(entry, ObjectEntry.class);
		assertNotNull(obj);

		assertEquals(name, obj.getName());
		assertEquals(typeName, obj.getTypeName());
	}

	private void AssertList(TypeEntry entry, String name, String typeName) {
		var list = TypeUtil.as(entry, ListEntry.class);
		assertNotNull(list);

		assertEquals(list.getName(), name);
		assertEquals(list.getTypeName(), typeName);
	}

}
