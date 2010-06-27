package controllers;

/**
 * About controller, handles /about URLs
 */
public class About extends SmartController {
	/**
	 * Default /about action, shows image of smartsoft with title on each
	 * developer
	 */
	public static void index() {
		String[] data = { "Amr Hany (C2), the CRUD guru!<br />Amr, the man behind the chat system, is akbar men keda bekter", // Amr
		// Hany
		"Ahmed Khaled (C4)<br />Ask him anything and he'll say: &#147;fol&#148;", // 
		"Mina Zaki (C2)<br />Mina introduced us to the accordion.<br />Things have become musical even since then.", // 
		"Mahmoud Sharawi (C3)<br />", // 
		"Joseph ElHajj (C5)<br />Joe speaks Japanese, and watches cartoons.<br />(Even during meetings!)", // 
		"Hossam Sharaf (C2)<br />", // 
		"Mohamed Monayri (C3)<br />One of the people behind the estimation game!", // 
		"Moataz Mekki (C1)<br />Moataz is notifying everyone!", // 
		"Omar Nabil (C1)<br />", // 
		"Hossam Amer (C4)<br />Hossam was working on the review log.<br />We still don't know what that is.<br />Just kidding. We do. Really. (Just don't ask.)", // 
		"Amr Othman (C1)<br />Wallas logs everything.", // 
		"Moumen Mohamed (C3)<br />Moumen took care of task types.<br />He's also very tall. Really really tall.", // 
		"Galal Aly (C3-lead)<br />Galal is also one of the people behind the estimation game.<br /> He's also mesh moktane3.", // 
		"Ahmed Behairy (C2)<br />He approves project requests. Really.", // 
		"Saher (scrum-master)<br />He made sure we followed the rules.<br />He also brought cake. Lots and lots of it.", // 
		"Amr Abdelwahab (C5)<br />Amr took snapshots. During meetings, after sprints.<br />In fact, he still is taking snapshots.", // 
		"Mahmoud Sakr (C1-lead, Manager)<br />Mahmoud is also known as ela2ra3<br />Unfortunately, it is true.", // 
		"Menna Ghoneim (C4-lead)<br /> You'll never find the AC off when she's around!<br />She wouldn't mind the food going cold, too.", // 
		"Dina Helal (C5-lead)<br />Dina dragged and dropped.<br />She would have dropped a rock on cartoons if she could.", // 
		"Heba Sherif (C3)<br />Heba, our best reviewer, loves sequence diagrams", //
		"Hadeer Diwan (C5)<br />Hadeer laid down the board back-end structure.<br />She didn't miss a chance to get mad at cartoons, too.", // 
		"Hadeer Younis (C4)<br />Hadeer made our site look pretty.<br />She's also very strong indeed!", // 
		"Dr Fatma Meawad (CEO)<br />Dr Fatma didn't only request a whole lot,<br />she also made us change it all (A)", // 
		"Asmaa Alkomy (C5)<br />Asmaa made dish parties come true.<br />She is really in C5. I promise.", // 
		"Ghada Fakhry (C2-lead)<br />Ghada had to put up with ALL our ERD issues<br />She also has the best ringtone ever!", // 
		};

		render(null, data);
	}
}
