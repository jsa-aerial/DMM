# From audio synthesis with unit generators to synthesis of animations and other forms of programming via streams of V-values.

Audio synthesis via **unit generators** was invented by Max Matthews 60 years ago:

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

In fact, looking at our https://arxiv.org/abs/1601.01050 one see a lot of similarities
between the previous paragraph and that preprint.


## Explicit unit generators in neural networks.

Any conventional neural network is built from unit generators (although control of the coefficients
tends to be external, with rare exceptions, and unit generators involved tend to be simpler
than the musical ones). 

However, we are seeing fruitful instances of
more traditional unit generators being injected into neural networks, see e.g.

https://arxiv.org/abs/1610.09513 , "Phased LSTM: Accelerating Recurrent Network Training
for Long or Event-based Sequences" by Neil et al.

## Visual synthesis and other kinds of programming via streams of V-values.

In this section we discuss synthesis via unit generators based on streams of V-values.

What is desirable is to handle other types of synthesis such as visual synthesis and
other types of programming with streams via similar discipline of compositional
functional programming with unit generators. 

However, streams of numbers are not sufficiently expressive for that. In general, one
would like to have streams of sufficiently expressive data structures here, and
V-values fit this role well.

[to be continued]

[to be continued]
