# From audio synthesis with unit generators to synthesis of animations and other forms of programming via streams of V-values.

Audio synthesis via **unit generators** was invented by Max Mathews 60 years ago:

https://en.wikipedia.org/wiki/MUSIC-N

This is a discipline of compositional functional programming
with streams of samples (streams of numbers in case of mono).
An introduction I particularly like is "Sonifying Processing:
The Beads Tutorial" by Evan X. Merz: http://evanxmerz.com/?page_id=18

(A free PDF: http://www.computermusicblog.com/SonifyingProcessing/Sonifying_Processing_The_Beads_Tutorial.pdf )

If one connects the unit generators via numerical coefficients (that is, via gain units or crossfaders), 
the numbers here play the role similar to the role of coefficients in neural nets. It is also frequent
to drive those numbers/coefficients by other unit generators, effectively making these coefficients
**higher-order**. The typical neurons are more powerful than neurons in conventional neural nets, often
with multiple input slots. 

In fact, looking at our https://arxiv.org/abs/1601.01050 one sees a lot of similarities
between the previous paragraph and that preprint.


## Explicit unit generators in neural networks.

Any conventional neural network is built from unit generators (although control of the coefficients
tends to be external, with rare exceptions, and unit generators involved tend to be simpler
than the musical ones). 

However, we are seeing fruitful instances of
more traditional unit generators being injected into neural networks, see e.g.

https://arxiv.org/abs/1610.09513 , "Phased LSTM: Accelerating Recurrent Network Training
for Long or Event-based Sequences" by Neil et al. In that paper, periodic sequences of
sparsely placed shapes known in audio synthesis as "Attack-Decay Envelopes" are injected
into particular locations of the network; the parameters of those sequences are trainable.

## Visual synthesis and other kinds of programming via streams of V-values.

In this section we discuss synthesis via unit generators based on streams of V-values.

What is desirable is to handle other types of synthesis such as visual synthesis and
other types of programming with streams via similar discipline of compositional
functional programming with unit generators. 

However, streams of numbers are not sufficiently expressive for that. In general, one
would like to have streams of sufficiently expressive data structures here, and
V-values fit this role well.

### Visual synthesis.

For example, if one has an animation with moving ellipses changing color as in 

https://github.com/anhinga/fluid/tree/master/quil-auxiliary/quil-try

one does not want to program in terms of streams of rectangular matrixes
of pixels representing those ellipses over transparent background, at least
not at the initial stages of the processing pipeline, but one wants instead to
program in terms of streams of compact data structures containing the
attributes defining the colored ellipse at a given moment of time.

Similarly, if one looks at the question of more effective implementation of

https://github.com/anhinga/fluid/blob/master/may_9_15_experiment/custom_wave_transform.pde

one would probably like to think in terms of streams of
mappings of `(i - center_x)^2 + (j - center_y)^2` integers to their assocated 
`mod` values as an important intermediate stream of V-values.

### Data structures.

Our recent experiments such as July 13 experiment suggest that streams of
V-values might be sufficient to express algorithms processing conventional
data structures (at least, if one agrees to process those data structures in immutable, but
non-lazy fashion, which seems to be a particularly natural fit for this
kind of architecture).



### Streams of V-values and dataflow matrix machines.

Dataflow matrix machines (DMMs) interleave non-linear and linear computations
(the computations in this case are computations with streams of V-values). 
This makes DMMs generalized neural networks and allows to continuously transform
any program written as a DMM into any other program written as a DMM by
continuously changing the linear part.

However, if these strong properties are not required, one does not have to
interleave non-linear and linear computations. Instead, one can just program
compositionally with unit generators based on streams of V-values, for example,
following the styles presented in the "Sonifying Processing" tutorial, only inserting
linear transformations such as gains and crossfaders in those places
where flexibility is required.

Therefore, maintaining the DMM discipline of interleaving non-linear and linear
transformations is an orthogonal concern to the discipline of synthesis via
unit generators based on streams of V-values. If one does interleave, one has more
flexibility and the design is much more open-ended.
