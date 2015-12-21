package eu.etaxonomy.cdm.io.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.io.markup.FeatureSorter;

public class FeatureSorterTest {

	static UUID uuid1a = UUID.randomUUID();
	static UUID uuid1b = UUID.randomUUID();
	static UUID uuid1c = UUID.randomUUID();
	static UUID uuid1d = UUID.randomUUID();
	static UUID uuid1e = UUID.randomUUID();
	static UUID uuid2a = UUID.randomUUID();
	static UUID uuid2b = UUID.randomUUID();
	static UUID uuid2c = UUID.randomUUID();
	static UUID uuid2c_c1 = UUID.randomUUID();
	static UUID uuid2c_c2 = UUID.randomUUID();
	static UUID uuid2d = UUID.randomUUID();
	static UUID uuid3a = UUID.randomUUID();
	static UUID uuid3b = UUID.randomUUID();
	static UUID uuid4a = UUID.randomUUID();
	static UUID uuid4b = UUID.randomUUID();
	static UUID uuid4c = UUID.randomUUID();
	static UUID uuid4d = UUID.randomUUID();
	
	static UUID[] uuidList1;
	static UUID[]  uuidList2;
	static UUID[]  uuidList3;
	static UUID[]  uuidList4;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		uuidList1 = new UUID[]{uuid1a, uuid1b, uuid1c, uuid1d, uuid4c};
		uuidList2 = new UUID[]{uuid1a, uuid2a, uuid2c, uuid4d};
		uuidList3 = new UUID[]{uuid2b, uuid2c, uuid1c, uuid4d};
		uuidList4 = new UUID[]{uuid1b, uuid2c, uuid2a};
//		printArray(uuidList1);
//		printArray(uuidList2);
//		printArray(uuidList3);
//		printArray(uuidList4);
	}

	@Before
	public void setUp() throws Exception {		

	}

	@Test
	public void testGetSortOrder() {
		FeatureSorter sorter = new FeatureSorter();
		Map<String,List<FeatureSorterInfo>> orderLists = getOrderLists();
		List<UUID> result = sorter.getSortOrder(orderLists);
//		System.out.println("A:" + result);
		int i = 0;
		Assert.assertEquals("uuid1a should be first", uuid1a, result.get(i++));
		Assert.assertEquals("uuid1b should be next", uuid1b, result.get(i++));
		Assert.assertEquals("uuid2b should be next", uuid2b, result.get(i++));
//		Assert.assertEquals("uuid1c should be next", uuid1c, result.get(i++));
		

		
	}

	@Test
	public void testGetSortOrderWithChildren() {
		FeatureSorter sorter = new FeatureSorter();
		Map<String,List<FeatureSorterInfo>> orderLists = getOrderLists();
		addChildren(orderLists);
		List<UUID> result = sorter.getSortOrder(orderLists);
//		System.out.println("B:" + result);
		int i = 0;
		Assert.assertEquals("uuid1a should be first", uuid1a, result.get(i++));
		Assert.assertEquals("uuid1b should be next", uuid1b, result.get(i++));
		Assert.assertEquals("uuid2b should be next", uuid2b, result.get(i++));
//		Assert.assertEquals("uuid1c should be next", uuid1c, result.get(i++));

		Assert.assertEquals(2 + getAllUuids().size(), result.size());
	}

	
	private Set<UUID> getAllUuids() {
		Set<UUID> all = new HashSet<UUID>();
		List<UUID[]> arrayList = new ArrayList<UUID[]>();
		arrayList.add(uuidList1);
		arrayList.add(uuidList2);
		arrayList.add(uuidList3);
		arrayList.add(uuidList4);
		for (UUID[] array : arrayList){
			for (UUID uuid: array){
				all.add(uuid);
			}
		}
		return all;
	}

	private static void printArray(UUID[] uuidArray) {
		List<UUID> list = new ArrayList<UUID>();
		for (UUID uuid : uuidArray){
			list.add(uuid);
		}
		System.out.println(list);
	}

	private void addChildren(Map<String, List<FeatureSorterInfo>> orderLists) {
		orderLists.get("2").get(1).addSubFeature(new FeatureSorterInfo(uuid2c_c1));
		orderLists.get("2").get(1).addSubFeature(new FeatureSorterInfo(uuid2c_c2));
		
	}

	private Map<String, List<FeatureSorterInfo>> getOrderLists() {
		Map<String,List<FeatureSorterInfo>> orderLists = new HashMap<String, List<FeatureSorterInfo>>();
		orderLists.put("1", getFeatureSorterInfoList(uuidList1));
		orderLists.put("2", getFeatureSorterInfoList(uuidList2));
		orderLists.put("3", getFeatureSorterInfoList(uuidList3));
		orderLists.put("4", getFeatureSorterInfoList(uuidList4));
		return orderLists;
	}

	private List<FeatureSorterInfo> getFeatureSorterInfoList(UUID[] uuidList) {
		List<FeatureSorterInfo> result= new ArrayList<FeatureSorterInfo>();
		for(UUID uuid : uuidList){
			result.add(new FeatureSorterInfo(uuid));
		}
		return result;
	}

}
