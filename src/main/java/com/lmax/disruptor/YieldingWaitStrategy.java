/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor;


/**
 * Yielding strategy that uses a Thread.yield() for {@link com.lmax.disruptor.EventProcessor}s waiting on a barrier
 * after an initially spinning.
 *
 * This strategy is a good compromise between performance and CPU resource without incurring significant latency spikes.
 */
public final class YieldingWaitStrategy implements WaitStrategy
{
    private static final int SPIN_TRIES = 100;

    @Override
    public long waitFor(final long sequence, Sequence cursor, final Sequence dependentSequence, final SequenceBarrier barrier)
        throws AlertException, InterruptedException
    {
        long availableSequence;
        int counter = SPIN_TRIES;
        //如果当前生产者序号大于消费者需要，就自旋
        while ((availableSequence = dependentSequence.get()) < sequence)
        {
            counter = applyWaitMethod(barrier, counter);
        }

        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking()
    {
    }

    private int applyWaitMethod(final SequenceBarrier barrier, int counter)
        throws AlertException
    {
        //counter 自旋临界值，默认100 ，等于0之后，当前线程让步，让下一个线程继续来自旋
        barrier.checkAlert();

        if (0 == counter)
        {
            Thread.yield();
        }
        else
        {
            --counter;
        }

        return counter;
    }
}
