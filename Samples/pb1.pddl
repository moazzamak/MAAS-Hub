(define (problem pb1)
  (:domain tea)
  (:requirements :strips :typing :negative-preconditions)
  (:objects
   water - pourable
   cup kettle - container
   milk - carton
   hand
   sugar
   tea_bag
   )

   (:init 
    (empty hand) 
    
    ;SOMETHING WRONG WITH THE TYPES
    ;(inside_of water kettle) 
    ;(empty cup) 
    ;(on_flat_surface cup)
    )


   (:goal (and (inside_of tea_bag cup)
	        (inside_of sugar cup)
	        (inside_of water cup)
			(hot water)
	        (on_flat_surface cup)
	        )
   )
)
