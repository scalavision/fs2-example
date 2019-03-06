package io.monadplus

import cats.effect.Concurrent
import fs2._
import fs2.concurrent.Queue

class Prefetch {
  def prefetch[F[_], F2[x] >: F[x]: Concurrent, O](s: Stream[F, O])(n: Int): Stream[F2, O] =
    prefetchN[F, F2, O](s)(1)

  def prefetchN[F[_], F2[x] >: F[x]: Concurrent, O](s: Stream[F, O])(n: Int): Stream[F2, O] =
    Stream.eval(Queue.bounded[F2, Option[Chunk[O]]](n)).flatMap { queue =>
      queue.dequeue.unNoneTerminate
        .flatMap(Stream.chunk)
        .concurrently(s.chunks.noneTerminate.covary[F2].through(queue.enqueue))
    }
}