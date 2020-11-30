package tech.pegasys.teku.ssz.backing.tree;

import tech.pegasys.teku.ssz.backing.tree.GIndexUtil.NodeRelation;

class TillIndexVisitor implements TreeVisitor {

  static TreeVisitor create(TreeVisitor delegate, long tillGeneralizedIndex) {
    return new TillIndexVisitor(delegate, tillGeneralizedIndex, true);
  }

  private final TreeVisitor delegate;
  private final long tillGIndex;
  private final boolean inclusive;

  public TillIndexVisitor(TreeVisitor delegate, long tillGIndex, boolean inclusive) {
    this.delegate = delegate;
    this.tillGIndex = tillGIndex;
    this.inclusive = inclusive;
  }

  @Override
  public boolean visit(TreeNode node, long generalizedIndex) {
    NodeRelation compareRes = GIndexUtil.gIdxCompare(generalizedIndex, tillGIndex);
    if (inclusive && compareRes == NodeRelation.Right) {
      return false;
    } else if (!inclusive && (compareRes == NodeRelation.Same)) {
      return false;
    } else {
      return delegate.visit(node, generalizedIndex);
    }
  }
}
