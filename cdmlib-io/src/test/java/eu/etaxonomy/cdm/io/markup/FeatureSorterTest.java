package eu.etaxonomy.cdm.io.markup;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.etaxonomy.cdm.io.markup.FeatureSorter;

public class FeatureSorterTest {

	UUID uuid1a = UUID.randomUUID();
	UUID uuid1b = UUID.randomUUID();
	UUID uuid1c = UUID.randomUUID();
	UUID uuid1d = UUID.randomUUID();
	UUID uuid1e = UUID.randomUUID();
	UUID uuid2a = UUID.randomUUID();
	UUID uuid2b = UUID.randomUUID();
	UUID uuid2c = UUID.randomUUID();
	UUID uuid2d = UUID.randomUUID();
	UUID uuid3a = UUID.randomUUID();
	UUID uuid3b = UUID.randomUUID();
	UUID uuid4a = UUID.randomUUID();
	UUID uuid4b = UUID.randomUUID();
	UUID uuid4c = UUID.randomUUID();
	UUID uuid4d = UUID.randomUUID();
	
	UUID[] uuidList1;
	UUID[]  uuidList2;
	UUID[]  uuidList3;
	UUID[]  uuidList4;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {		
		uuidList1 = new UUID[]{uuid1a, uuid1b, uuid1c, uuid1d, uuid4c};
		uuidList2 = new UUID[]{uuid1a, uuid2a, uuid2c, uuid4d};
		uuidList3 = new UUID[]{uuid2b, uuid2c, uuid1c, uuid4d};
		uuidList4 = new UUID[]{uuid1b, uuid2c, uuid2a};
	}

	@Test
	public void testGetSortOrder() {
		FeatureSorter sorter = new FeatureSorter();
		Map<String,List<FeatureSorterInfo>> orderLists = getOrderLists();
		sorter.getSortOrder(orderLists);
		
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
