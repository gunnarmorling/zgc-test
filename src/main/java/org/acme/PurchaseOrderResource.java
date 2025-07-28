package org.acme;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/purchase_orders")
public class PurchaseOrderResource {

	private static final int OUTER_SIZE = Integer.valueOf(Optional.ofNullable(System.getenv("RANDOM_COUNT")).orElse("100"));
	private static final int INNER_SIZE = 1000;

    private static final int PURCHASE_ORDER_COUNT = 100;

	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public PurchaseOrder getPurchaseOrder(@PathParam("id") long id) {
        return PurchaseOrder.findById(id);
    }

	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/random")
    public PurchaseOrder getRandomPurchaseOrder() {
		PurchaseOrder po = PurchaseOrder.findById(ThreadLocalRandom.current().nextInt(PURCHASE_ORDER_COUNT - 1) + 1);

		List<List<Long>> randoms = new ArrayList<>(OUTER_SIZE);
		for(int i = 0; i < OUTER_SIZE; i++) {
			List<Long> l = new ArrayList<>(INNER_SIZE);
			for(int j = 0; j < INNER_SIZE; j++) {
				l.add(ThreadLocalRandom.current().nextLong());
			}
			randoms.add(l);
		}

		po.random = randoms.get(ThreadLocalRandom.current().nextInt(randoms.size())).toString();
        return po;
    }

    @Transactional
    public void onStartup(@Observes StartupEvent ev) {
    	List<Product> products = List.of(
    		persistProduct("Socks", "A sock is a piece of clothing worn on the feet and often covering the ankle or some part of the calf. Some types of shoes or boots are typically worn over socks. In ancient times, socks were made from leather or matted animal hair. Machine-knit socks were first produced in the late 16th century. Until the 1800s, both hand-made and machine-knit socks were manufactured, with the latter technique becoming more common in the 19th century, and continuing until the modern day. "),
	    	persistProduct("Hammer", "A hammer is a tool, most often a hand tool, consisting of a weighted \"head\" fixed to a long handle that is swung to deliver an impact to a small area of an object. This can be, for example, to drive nails into wood, to shape metal (as with a forge), or to crush rock.[1][2] Hammers are used for a wide range of driving, shaping, breaking and non-destructive striking applications. Traditional disciplines include carpentry, blacksmithing, warfare, and percussive musicianship (as with a gong). "),
	    	persistProduct("Screws", "A screw is an externally helical threaded fastener capable of being tightened or released by a twisting force (torque) to the head. The most common uses of screws are to hold objects together and there are many forms for a variety of materials. Screws might be inserted into holes in assembled parts or a screw may form its own thread.[1] The difference between a screw and a bolt is that the latter is designed to be tightened or released by torquing a nut. "),
	    	persistProduct("Shirt", "A shirt is a cloth garment for the upper body (from the neck to the waist). Originally an undergarment worn exclusively by men, it has become, in American English, a catch-all term for a broad variety of upper-body garments and undergarments. In British English, a shirt is more specifically a garment with a collar, sleeves with cuffs, and a full vertical opening with buttons or snaps (North Americans would call that a \"dress shirt\", a specific type of collared shirt). A shirt can also be worn with a necktie under the shirt collar. "),
	    	persistProduct("Rubber duck", "A rubber duck, or a rubber duckie, is a toy shaped like a duck, that is usually yellow with a flat base. It may be made of rubber or rubber-like material such as vinyl plastic.[1] Rubber ducks were invented in the late 19th century when it became possible to more easily shape rubber,[2] and are believed to improve developmental skills in children during water play. The yellow rubber duck has achieved an iconic status in Western pop culture and is often symbolically linked to bathing. Various novelty variations of the toy are produced, and many organisations use yellow rubber ducks in rubber duck races for fundraising worldwide. "),
	    	persistProduct("Glasses", "Glasses, also known as eyeglasses, spectacles, or colloquially as specs, are vision eyewear with clear or tinted lenses mounted in a frame that holds them in front of a person's eyes, typically utilizing a bridge over the nose and hinged arms, known as temples or temple pieces, that rest over the ears for support. Glasses are typically used for vision correction, such as with reading glasses and glasses used for nearsightedness; however, without the specialized lenses, they are sometimes used for cosmetic purposes. ")
    	);

    	List<String> customers = List.of("Bob", "Barry", "Saundra", "Timothy", "Sarah", "Jim", "Jean", "Pascal");
    	for (int i = 0; i < PURCHASE_ORDER_COUNT; i++) {
    		persistPurchaseOrder(customers.get(ThreadLocalRandom.current().nextInt(customers.size())), products);
    	}
    }

    private void persistPurchaseOrder(String customer, List<Product> allProducts) {
    	PurchaseOrder order = new PurchaseOrder();
    	order.customer = customer;
    	order.orderLines = new ArrayList<OrderLine>();

    	for(int i = 0; i < 3; i++) {
    		OrderLine orderLine = new OrderLine();
    		orderLine.quantity = ThreadLocalRandom.current().nextInt(6);
    		orderLine.price = new BigDecimal(ThreadLocalRandom.current().nextDouble(100.00)).setScale(2, RoundingMode.HALF_EVEN);
    		orderLine.item = allProducts.get(ThreadLocalRandom.current().nextInt(allProducts.size()));

    		order.orderLines.add(orderLine);
    		orderLine.purchaseOrder = order;
    	}

    	order.persist();

	}

	private Product persistProduct(String name, String description) {
    	Product product = new Product();
    	product.name = name;
    	product.description = description;
    	product.persist();

    	return product;
    }
}
