How does it work?

- com.vhausler.ps.controller.SRealityController can be run and based on the parameters in its start method it scrapes property prices for different cities (or even the whole country). There's no jar with dependencies and such, it's run from
  the IDE at the moment.
- Loads a page with 404 result to work its way around consent cookies.
- Loads a random page with results (waits for them dynamically, common theme for all) to adjust the result size from 20 to 60 per page (faster scraping).
- Loads the page it really wants to scrape and parses the total number of results to calculate the number of pages it has to go through.
- Goes through all the pages by adjusting url instead of having to click buttons in the paging section. This sounds like a minor thing, but it potentially enables the scraping of as many pages in parallel as the site and your machine can
  handle if done correctly (websites are the usual bottleneck). This is just a note, there was no need to implement this feature as there's usually less than 25 k results for the whole country. But I've done this in the past.
- Collects all data scraped from the website and outputs it as an Excel file which I can distribute further.
- Note: Uses "full" browser for debugging purposes, but in a higher perf scraping environment the browser can be limited to just its header/title part to limit the amount of resources it eats. This is insignificant for a project like this,
  but major for massive parallel scrapers.

More technical notes

- ~ 4 - 6 hrs of work, most of which was spent on site-specific issues (cookies, dynamic waiting, finding the correct elements)

Avoiding consent via cookies

- sreality.cz redirects to seznam.cz for GDPR consent, cookie can't be set for the domain sreality.cz while on seznam.cz, the workaround for this is to access any page on sreality.cz domain which loads fast and doesn't trigger the consent
  redirect and set the cookie there.

Dynamic waiting

- There seems to be an async call to load the properties, so a dynamic wait had to be implemented, otherwise the property list element lookup would find nothing (common theme for sites with async calls).

Paging workaround through URL

- Paging through certain sites using buttons can be very frustrating as some of them don't respond to click(), some are fake buttons hooked up to JS and such. Luckily sreality.cz uses 'strana' query param in the URL, so all I had to do was
  calculate the number of pages the scraper has to go through.

Messed up source data

- sreality.cz uses 2 different whitespaces which made parsing the square meters and prices more difficult. Parsing could've been done more efficiently (even Sonar suggested that), but the performance bottleneck is elsewhere, so no
  optimization here was implemented.
- Some property offers are missing prices, some have extra words in the title.

Excel export

- I could've mapped the values manually, it would've been faster at the time, but I'm too lazy to go back and fix the mapping whenever I want to add a new column to the Excel file, so the implementation is more dynamic using reflection (
  header = field name, value = field value), it cost more time to implement, but I often choose this "lazy" approach - more time upfront, save time later.
- Can be even more abstract - generic Excel export object with annotations to be able to override the header names.
- Integer cell value is important to help Excel treat the values correctly.

Notes

- Currency as integer and not BigDecimal - intentional. The idea here is statistics, not exact values. Which reminds me that I've never worked with currency on any commercial project.
- Can be easily reworked to support different sites with certain parts of code being common. I've done this in the past for Deloitte on a much bigger scale.
- I've skipped unit tests and anything that would hinder me speed wise. The main goal was to create a stable scraper for property prices ASAP.
- No DDoS or security dodging needed. Seznam used to have security measures on certain pages like firmy.cz I believe, where they would ban your IP address for a short amount of time if you've made too many requests in a short time. The
  workaround for sites like that was to use free third level proxies (they pretend to be you) around the world to dodge bans in order to avoid downtimes.