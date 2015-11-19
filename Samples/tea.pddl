(define (domain tea )
	(:requirements :strips :typing :negative-preconditions)
	(:types container carton location pourable)
	(:predicates
		(held ?Obj)
		(empty ?Obj)
		(inside_of ?Obj ?c - container)
		(on_flat_surface ?Obj - container)
		(hot ?Obj)
		(uncapped ?Obj - carton)
        (at ?Obj ?place - location)
		(reachable ?Obj - container)
	)

	(:action pour_into
		:parameters ( ?Obj
		;- pourable
		?container )
		:precondition ( and (held ?Obj) 
						(on_flat_surface ?container) 
						(not (empty ?Obj)) 
						(empty ?container) 
						(reachable ?container) 
						(uncapped ?Obj) ) 
		:effect ( and ( not (empty ?container))
						(inside_of ?Obj ?container) 
						(empty ?Obj) )
	)
		
	(:action put_in
		:parameters( ?Obj ?container )
		:precondition ( and (held ?Obj) 
						(on_flat_surface ?container) 
						(reachable ?container) )
		:effect ( inside_of ?Obj ?container )
	)
	(:action pick
		:parameters( ?place - location ?robot ?hand ?Obj )
		:precondition ( and (empty ?hand) 
						(at ?robot ?place) 
						(reachable ?Obj) )
		:effect( and (held ?Obj) 
						(not (at ?Obj ?place))
					    (not (empty ?hand))) 
	)		
	(:action heat
		:parameters ( ?Obj ?container )
		:precondition ( not ( empty ?container ) )
		:effect ( hot ?Obj )
	)

	(:action move
		:parameters ( ?from - location ?to - location ?robot) 
		:precondition ( at ?robot ?from)
		:effect ( at ?robot ?to ) 
	)

	(:action remove_cap
		:parameters ( ?Obj )
		:precondition (and (uncapped ?Obj) 
						(held ?Obj) )
		:effect (uncapped ?Obj) 
	)

	(:action replace_cap
		:parameters ( ?Obj )
		:precondition (and (uncapped ?Obj) 
						(held ?Obj) )
		:effect (not (uncapped ?Obj)) 
	)
		
	(:action carry
		:parameters (?Obj ?from - location ?to - location ?robot)
		:precondition (and (held ?Obj) 
						(at ?robot ?from))
		:effect (at ?robot ?to) 
	)
		
	(:action refill_container
		:parameters ( ?tap - location ?container ?robot ?Obj )
		:precondition (and (held ?container) 
						(at ?robot ?tap) 
						(empty ?container))
		:effect (and (not (empty ?container)) 
						(inside_of ?Obj ?container)) 
	)
		
	(:action restock
		:parameters ( ?carton ?store - location ?hand ?robot)
		:precondition (and (at ?robot ?store) 
						(empty ?carton) 
						(empty ?hand))
		:effect (and (held ?carton) 
						(not (empty ?hand)) 
						(not (empty ?carton)))
	)
)
