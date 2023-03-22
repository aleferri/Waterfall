# Waterfall
Waterfall is a library for flow evaluation

# Genericized
The library act without knowing the full concrete types used, callbacks receive back the exact type given. You are required to write your own conforming implementation.

You need an E enum that specify a finite number of stage types/kinds, a S stage class that extends FlowStage<E> and a L link class that extends Link. You then need to implement:

Plan<E, S, L>, Dispatcher<E, S> and CallbackTable<E, S>.

You may create a multi generation (Wave) flow using the appropriate API inside your implementation of CallbackTable, allowing you to keep track of parent and child stages.
