package com.github.eddieburns55.intervaltree;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

/*
 * An immutable implementation of an IntervalTree. An IntervalTree allows you to query which intervals 
 * overlap a given point. 
 * 
 * Multiple intervals may overlap. Only unique intervals are allowed -- duplicate intervals are combined.
 * 
 * For now, only inclusive endpoints are allowed (i.e. both endpoints are "closed", in Guava terminology.)
 * 
 * Ranges of type BigInteger and Byte are allowed but NOT recommended -- they may result in exceptions or undefined behavior.
 */
public class IntervalTree <C extends Number & Comparable<C>> {
	private final IntervalTree<C>.IntervalTreeNode root;

	public IntervalTree(final Collection<Range<C>> ranges){
		final IntervalTreeNode root = buildTree(ranges);
		this.root = root;
	}
	
	/*
	 * Sorts by the lower endpoint in the range. Sorts smallest to largest.
	 */
	final Comparator<Range<C>> lowerEndpointSorter = new Comparator<Range<C>>() {
		@Override
		public int compare(Range<C> r1, Range<C> r2) {
			if (r1.lowerEndpoint().doubleValue() < r2.lowerEndpoint().doubleValue()) { return -1; }
			if (r1.lowerEndpoint().doubleValue() > r2.lowerEndpoint().doubleValue()) { return 1; }
			else { return 0; }
		}
	};
	
	/*
	 * Sorts by the upper endpoint in the range. Sorts biggest to smallest.
	 */
	final Comparator<Range<C>> upperEndpointSorter = new Comparator<Range<C>>() {
		@Override
		public int compare(Range<C> r1, Range<C> r2) {
			if (r1.upperEndpoint().doubleValue() < r2.upperEndpoint().doubleValue()) { return 1; }
			if (r1.upperEndpoint().doubleValue() > r2.upperEndpoint().doubleValue()) { return -1; }
			else { return 0; }
		}
	};
	
	private class IntervalTreeNode {
		private final double median;
		private final IntervalTreeNode left;
		private final IntervalTreeNode right;
		private final Collection<Range<C>> intersecsSortedByLower;
		private final Collection<Range<C>> intersecsSortedByUpper;

		private IntervalTreeNode(
				double median, 
				Collection<Range<C>> intersecsSortedByLower, 
				Collection<Range<C>> intersecsSortedByUpper, 
				IntervalTreeNode left, 
				IntervalTreeNode right){
			
			this.median = median;
			this.intersecsSortedByLower = intersecsSortedByLower;
			this.intersecsSortedByUpper = intersecsSortedByUpper;
			this.left = left;
			this.right = right;
		}
	}
	
	/*
	 * Recursively builds the interval tree from the given ranges
	 */
	private IntervalTreeNode buildTree(final Collection<Range<C>> ranges){
		if (ranges.size() == 0){
			return null;
		}
		
		final double median = computeMedianOfRanges(ranges);
		final Set<Range<C>> leftOfMedian = new HashSet<>(); 
		final Set<Range<C>> rightOfMedian = new HashSet<>();
		final Set<Range<C>> intersectingMedian = new HashSet<>();
		getRangesLeftAndRightOfMedian(median, ranges, leftOfMedian, rightOfMedian, intersectingMedian);

		final IntervalTreeNode leftNode = buildTree(leftOfMedian);
		final IntervalTreeNode rightNode = buildTree(rightOfMedian);
		
		final TreeSet<Range<C>> intersectingSortedByLower = new TreeSet<>(lowerEndpointSorter);
		intersectingSortedByLower.addAll(intersectingMedian);
		
		final TreeSet<Range<C>> intersectingSortedByUpper = new TreeSet<>(upperEndpointSorter);
		intersectingSortedByUpper.addAll(intersectingMedian);
		
		final IntervalTreeNode node = new IntervalTreeNode(median, intersectingSortedByLower, intersectingSortedByUpper, leftNode, rightNode);
		return node;
	}
	
	public Set<Range<C>> searchForIntersectingRanges(final C searchVal){
		final HashSet<Range<C>> intersectingRanges = new HashSet<Range<C>>();
		doSearch(searchVal, root, intersectingRanges);
		return intersectingRanges;
	}
	
	/*
	 * Recursively finds all ranges that intersect with the point
	 */
	private void doSearch(final C searchVal, final IntervalTreeNode currentNode, /*mutates*/ final Set<Range<C>> intersectingRanges){
		if (currentNode == null){
			return;
		} 
		
		final double searchValDouble = searchVal.doubleValue();
		if (searchValDouble == currentNode.median){
			intersectingRanges.addAll(currentNode.intersecsSortedByLower);
		} else if (searchValDouble < currentNode.median){ //search left
			for (Range<C> range : currentNode.intersecsSortedByLower){
				if (searchValDouble < range.lowerEndpoint().doubleValue() || searchValDouble > range.upperEndpoint().doubleValue()){
					break;
				} else if (searchValDouble >= range.lowerEndpoint().doubleValue() && searchValDouble <= range.upperEndpoint().doubleValue()){
					intersectingRanges.add(range);
				} else {
					//shouldn't ever reach here
				}
			}
			doSearch(searchVal, currentNode.left, intersectingRanges);
		} else if (searchValDouble > currentNode.median){ //search right
			for (Range<C> range : currentNode.intersecsSortedByUpper){
				if (searchValDouble < range.lowerEndpoint().doubleValue() || searchValDouble > range.upperEndpoint().doubleValue()){
					break;
				} else if (searchValDouble >= range.lowerEndpoint().doubleValue() && searchValDouble <= range.upperEndpoint().doubleValue()){
					intersectingRanges.add(range);
				} else {
					//shouldn't ever reach here
				}
			}			
			doSearch(searchVal, currentNode.right, intersectingRanges);
		}
	}
	
	/*
	 * Computes the median of the total range of all the given ranges
	 */
	private double computeMedianOfRanges(final Collection<Range<C>> ranges){
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		for (Range<C> r : ranges){
			if (!r.hasLowerBound() || !r.hasUpperBound()){
				throw new IllegalArgumentException("Ranges cannot be unbounded");
			}
			
			if (r.lowerEndpoint().doubleValue() < min) { 
				min = r.lowerEndpoint().doubleValue();
			} 
			if (r.upperEndpoint().doubleValue() > max){
				max = r.upperEndpoint().doubleValue();
			}
		}
		return (min + max)/2;
	}
	
	/*
	 * Separates the given ranges according to which side of the median that they fall on
	 */
	private void getRangesLeftAndRightOfMedian(
			final double median, 
			final Collection<Range<C>> ranges, 
			/*mutates*/ final Collection<Range<C>> leftRanges, 
			/*mutates*/ final Collection<Range<C>> rightRanges, 
			/*mutates*/ final Collection<Range<C>> intersectingRanges){
		
		for (Range<C> range : ranges){
			final double upperVal = range.upperEndpoint().doubleValue();
			final double lowerVal = range.lowerEndpoint().doubleValue();
			
			if (range.upperBoundType().equals(BoundType.OPEN) || range.lowerBoundType().equals(BoundType.OPEN)){
				throw new IllegalArgumentException("Ranges must be have inclusive (\"closed\") bounds");
			} else if (Double.isNaN(upperVal) || Double.isInfinite(upperVal) ||
				Double.isNaN(lowerVal) || Double.isInfinite(lowerVal)){
				throw new IllegalArgumentException("Range endpoints must be finite numbers");
			}
			
			if (median > upperVal){
				leftRanges.add(range);
			} else if (median < lowerVal){
				rightRanges.add(range);
			} else {
				intersectingRanges.add(range);
			}
		}
	}
}
