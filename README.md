# networking_proj

+ Midpoint Check: Oct 22
+ Due Dec 7

## Documentation

The following documentation describes the planned implementation
of the project.

The project consists of a few core constituent classes:

+ `Peer`
+ `Message` and its subclasses (`<name>Message`)
+ `Server`
+ `Client`

There are also a few smaller classes, used to load and store
configuration data:

+ `PeerConfiguration`
+ `CommonConfiguration`

These classes interact to form the overall architecture of
the project.

### Classes

The top-level class of the project is the `Peer` class.
`Peer` will implement the `main` function in `Peer::main`.
The `Peer` class constructs and invokes all other classes.
The execution flow of `Peer` will essentially be this:

+ `Peer::main` constructs a `Peer` and calls `Peer::run`. 
+ `Peer::main` calls `Peer::run`.
+ `Peer::run` calls `Peer::startup` to do any startup
processes (e.g. launching client and server threads), and
then goes into the execution loop.
+ The execution loop in `Peer::run` is a while loop which runs
until the `Peer` process is terminated (e.g. by a keyboard interrupt).
This loop will include the following steps:
    + It takes a message from the incoming message queue (`Peer::messageQueue`)
    + It then calls `Peer::handleMessage` to handle the message
based on the type and contents of the message.
+ This loop is repeated indefinitely until termination.
+ At termination, `Peer::shutdown` is invoked to clean up
any threads or data.

The `Client` class is used to receive messages over TCP
from other `Peer` objects in other processes/hosts. 
The `Client` is responsible for receiving string data over
TCP, converting this string data to a `Message` object using
the `MessageFactory` class, and adding this `Message` to
the `Peer` object's `messageQueue` (`Peer::messageQueue`).
The `Peer` instance running will maintain once `Client`
for each other peer in the network. Each `Client` is responsible
for communicating only with one other peer. Thus, the `Client`
objects exist as a member of the `Peer`. However, despite
this ownership relationship, the `Client` will need some way
to add `Message` objects to the message queues. The easiest way
to accomplish this may be to pass a lambda function which
adds messages to `Peer::messageQueue` to `Client` in its
constructor (`Client::Client()`).

The `Server` class is used simply to send data to other `Peer`
objects in other processes/hosts. The server is responsible
for receiving `Message` objects to send via some method
(`Server::send`?), serializing these messages to strings
using `Message::serialize`, and sending the resulting string
over TCP.

The `Message` class is an abstract base class from which all
message types derive. The derived types include:

+ `BitfieldMessage`
+ `ChokeMessage`
+ `HaveMessage`
+ `InterestedMessage`
+ `PieceMessage`
+ `RequestMessage`
+ `UnchokeMessage`
+ `UninterestedMessage`

All sub-classes implement the abstract methods of `Message`.
The `Message` class provides a single API from which messages
can be created by `Peer` using the various derived class constructors,
and then be serialized to strings to be sent over TCP
using `Message::serialize`. `Message::serialize` converts the contents of the message
object into bytes (which are encoded such that no information
is lost - see `StringEncoder.java`).

The `MessageFactory` is used to complete the opposite conversion:
from string to `Message` objects. The `MessageFactory` is an example
of the factory design pattern: see [here](https://en.wikipedia.org/wiki/Factory_method_pattern) for more
information. Briefly, the `MessageFactory` will take any valid serialized message string
and return the object of the correct sub-type (e.g. `BitfieldMessage`, `HaveMessage`, etc.).
The `MessageFactory` is used by the client to convert raw strings to `Message` objects.

The `PeerConfiguration` class serves two purposes:
(1) to load the peer configuration data from the `PeerInfo.cfg` and
(2) to store the network information of other peers
during the execution of the `Peer` class for sending and receiving
messages. This is why `Message` has a `PeerConfiguration` member:
to identify the sender *or* receiver of the `Message` (whether
it is sender or receiver is contextual).

The `CommonConfiguration` class server to load the data
from `Common.cfg` for the `Peer` class.