package io.zeebe.util.state.concurrent;

import io.zeebe.util.Loggers;
import org.agrona.BitUtil;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.slf4j.Logger;

/**
 * One central state machine manager that records and dispatches events for all state machines.
 */
public class SharedStateMachineManager<T>
{
    public static final Logger LOG = Loggers.STATE_MACHINE_LOGGER;

    protected UnsafeBuffer tempBuf = new UnsafeBuffer(new byte[BitUtil.SIZE_OF_INT]);
    protected static final int DISPATCHES_PER_ITERATION = 32;

    protected Int2ObjectHashMap<SharedStateMachine<T>> stateMachines = new Int2ObjectHashMap<>();
    protected ManyToOneRingBuffer transitionBuffer;

    public SharedStateMachineManager(ManyToOneRingBuffer transitionBuffer)
    {
        this.transitionBuffer = transitionBuffer;
    }

    public void register(SharedStateMachine<T> stateMachine)
    {
        stateMachines.put(stateMachine.getId(), stateMachine);
    }

    public void unregister(SharedStateMachine<T> stateMachine)
    {
        stateMachines.remove(stateMachine.getId());
    }

    public boolean recordStateChange(int stateMachineId, DirectBuffer buf)
    {
        return transitionBuffer.write(stateMachineId, buf, 0, BitUtil.SIZE_OF_INT);
    }

    public int dispatchTransitionEvents()
    {
        return transitionBuffer.read(this::dispatchTransitionEvent, DISPATCHES_PER_ITERATION);
    }

    protected void dispatchTransitionEvent(int stateMachineId, MutableDirectBuffer buffer, int offset, int length)
    {
        final SharedStateMachine<T> stateMachine = stateMachines.get(stateMachineId);

        if (stateMachine != null)
        {
            final int newState = buffer.getInt(offset);
            stateMachine.dispatchTransitionEvent(newState);
        }
        else
        {
            LOG.error("Could not dispatch event for state machine {};  not registered", stateMachineId);
        }
    }

    public SharedStateMachine<T> buildStateMachine(SharedStateMachineBlueprint<T> lifecycle, T managedObject)
    {
        return lifecycle.buildStateMachine(managedObject, this);
    }
}
