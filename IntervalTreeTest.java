package com.github.eddieburns55.intervaltree;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

public class IntervalTreeTest {
	@Test
	public void testIntegerCreationAndSearch(){
		@SuppressWarnings("unchecked")
		final Set<Range<Integer>> ranges = ImmutableSet.of(
				Range.closed(-10, 3),
				Range.closed(-2, 5),
				Range.closed(4, 10),
				Range.closed(5, 20),
				Range.closed(11, 14),
				Range.closed(500, 10000)
				);
		
		final IntervalTree<Integer> intervalTree = new IntervalTree<>(ranges);
		
		final Set<Range<Integer>> ranges3 = intervalTree.searchForIntersectingRanges(3);
		assertTrue(ranges3.size() == 2);
		assertTrue(ranges3.contains(Range.closed(-10, 3)));
		assertTrue(ranges3.contains(Range.closed(-2, 5)));
		
		final Set<Range<Integer>> ranges12 = intervalTree.searchForIntersectingRanges(12);
		assertTrue(ranges12.size() == 2);
		assertTrue(ranges12.contains(Range.closed(5,20)));
		assertTrue(ranges12.contains(Range.closed(11,14)));
		
		final Set<Range<Integer>> ranges200 = intervalTree.searchForIntersectingRanges(200);
		assertTrue(ranges200.size() == 0);
		
		final Set<Range<Integer>> rangesNeg4 = intervalTree.searchForIntersectingRanges(-4);
		assertTrue(rangesNeg4.size() == 1);
		assertTrue(rangesNeg4.contains(Range.closed(-10,3)));
		
		final Set<Range<Integer>> ranges499 = intervalTree.searchForIntersectingRanges(499);
		assertTrue(ranges499.size() == 0);
		
		final Set<Range<Integer>> ranges500 = intervalTree.searchForIntersectingRanges(500);
		assertTrue(ranges500.size() == 1);
		assertTrue(ranges500.contains(Range.closed(500, 10000)));
	}
	
	@Test
	public void testDoubleCreationAndSearch(){
		final Set<Range<Double>> ranges = ImmutableSet.of(
				Range.closed(-10.1, 3.5),
				Range.closed(-2.1, 5.1),
				Range.closed(4.1, 10.9),
				Range.closed(5.2, 20.4),
				Range.closed(11.3, 14.6)
				);
		
		final IntervalTree<Double> intervalTree = new IntervalTree<>(ranges);
		
		final Set<Range<Double>> ranges3 = intervalTree.searchForIntersectingRanges(4.09D);
		assertTrue(ranges3.size() == 1);
		assertTrue(ranges3.contains(Range.closed(-2.1, 5.1)));
		
		final Set<Range<Double>> ranges12 = intervalTree.searchForIntersectingRanges(12D);
		assertTrue(ranges12.size() == 2);
		assertTrue(ranges12.contains(Range.closed(5.2, 20.4)));
		assertTrue(ranges12.contains(Range.closed(11.3, 14.6)));
		
		final Set<Range<Double>> ranges200 = intervalTree.searchForIntersectingRanges(200D);
		assertTrue(ranges200.size() == 0);
		
		final Set<Range<Double>> rangesNeg4 = intervalTree.searchForIntersectingRanges(-4D);
		assertTrue(rangesNeg4.size() == 1);
		assertTrue(rangesNeg4.contains(Range.closed(-10.1, 3.5)));
	}
	
	@Test
	public void testDuplicates(){
		@SuppressWarnings("unchecked")
		final Set<Range<Integer>> ranges = ImmutableSet.of(
				Range.closed(-10, 3),
				Range.closed(-2, 5),
				Range.closed(4, 10),
				Range.closed(5, 20),
				Range.closed(11, 14),
				Range.closed(-10, 3),
				Range.closed(-2, 5),
				Range.closed(4, 10),
				Range.closed(5, 20),
				Range.closed(11, 14)
				);
		
		final IntervalTree<Integer> intervalTree = new IntervalTree<>(ranges);
		
		final Set<Range<Integer>> ranges3 = intervalTree.searchForIntersectingRanges(3);
		assertTrue(ranges3.size() == 2);
		assertTrue(ranges3.contains(Range.closed(-10, 3)));
		assertTrue(ranges3.contains(Range.closed(-2, 5)));
		
		final Set<Range<Integer>> ranges12 = intervalTree.searchForIntersectingRanges(12);
		assertTrue(ranges12.size() == 2);
		assertTrue(ranges12.contains(Range.closed(5,20)));
		assertTrue(ranges12.contains(Range.closed(11,14)));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOpen(){
		@SuppressWarnings("unchecked")
		final Set<Range<Integer>> ranges = ImmutableSet.of(
				Range.closed(-10, 3),
				Range.closed(-2, 5),
				Range.closed(4, 10),
				Range.closed(5, 20),
				Range.closed(11, 14),
				Range.closed(-10, 3),
				Range.closed(-2, 5),
				Range.closed(4, 10),
				Range.closed(5, 20),
				Range.atLeast(-2) //causes exception when building tree
				);
		
		final IntervalTree<Integer> intervalTree = new IntervalTree<>(ranges); //should result in exception
		
		final Set<Range<Integer>> ranges3 = intervalTree.searchForIntersectingRanges(3);
		assertTrue(ranges3.size() == 2);
		assertTrue(ranges3.contains(Range.closed(-10, 3)));
		assertTrue(ranges3.contains(Range.closed(-2, 5)));
	}
	
	@Test
	public void testLargeInputSet(){
		int count = 1000;
		final List<Range<Integer>> rangesList = new ArrayList<>();
		for (int i=0; i<count; i++){
			for (int j=0; j<count; j++){
				if (i <= j){
					rangesList.add(Range.closed(i, j));
				}
			}
		}
		final Set<Range<Integer>> ranges = new HashSet<>(rangesList);
		
		final IntervalTree<Integer> intervalTree = new IntervalTree<>(ranges);
		
		int sum = 0;
		for (int i=0; i<count; i++){
			final Set<Range<Integer>> intersectingRanges = intervalTree.searchForIntersectingRanges(i);
			intersectingRanges.size();
		}
		sum += sum;
	}
}
