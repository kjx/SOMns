package som.interpreter.nodes.specialized;

import som.compiler.Tags;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.PreevaluatedExpression;
import som.interpreter.nodes.nary.QuaternaryExpressionNode;
import som.vmobjects.SBlock;
import som.vmobjects.SInvokable;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;

import dym.Tagging;


public abstract class IntToByDoMessageNode extends QuaternaryExpressionNode
    implements PreevaluatedExpression {

  private final SInvokable blockMethod;
  @Child private DirectCallNode valueSend;

  public IntToByDoMessageNode(final ExpressionNode orignialNode,
      final SBlock block) {
    super(Tagging.cloneAndAddTags(orignialNode.getSourceSection(), Tags.LOOP_NODE));
    blockMethod = block.getMethod();
    valueSend = Truffle.getRuntime().createDirectCallNode(
                    blockMethod.getCallTarget());
  }

  public IntToByDoMessageNode(final IntToByDoMessageNode node) {
    super(Tagging.cloneAndAddTags(node.getSourceSection(), Tags.LOOP_NODE));
    this.blockMethod = node.blockMethod;
    this.valueSend   = node.valueSend;
  }

  @Override
  public final Object doPreEvaluated(final VirtualFrame frame,
      final Object[] arguments) {
    return executeEvaluated(frame, arguments[0], arguments[1],  arguments[2],
        arguments[3]);
  }

  protected final boolean isSameBlockLong(final SBlock block) {
    return block.getMethod() == blockMethod;
  }

  protected final boolean isSameBlockDouble(final SBlock block) {
    return block.getMethod() == blockMethod;
  }

  @Specialization(guards = "isSameBlockLong(block)")
  public final long doIntToByDo(final VirtualFrame frame, final long receiver, final long limit, final long step, final SBlock block) {
    try {
      if (receiver <= limit) {
        valueSend.call(frame, new Object[] {block, receiver});
      }
      for (long i = receiver + 1; i <= limit; i += step) {
        valueSend.call(frame, new Object[] {block, i});
      }
    } finally {
      if (CompilerDirectives.inInterpreter()) {
        SomLoop.reportLoopCount(limit - receiver, this);
      }
    }
    return receiver;
  }

  @Specialization(guards = "isSameBlockDouble(block)")
  public final long doIntToByDo(final VirtualFrame frame, final long receiver, final double limit, final long step, final SBlock block) {
    try {
      if (receiver <= limit) {
        valueSend.call(frame, new Object[] {block, receiver});
      }
      for (long i = receiver + 1; i <= limit; i += step) {
        valueSend.call(frame, new Object[] {block, i});
      }
    } finally {
      if (CompilerDirectives.inInterpreter()) {
        SomLoop.reportLoopCount((long) limit - receiver, this);
      }
    }
    return receiver;
  }

  @Override
  public boolean isResultUsed(final ExpressionNode child) {
    return false;
  }
}
