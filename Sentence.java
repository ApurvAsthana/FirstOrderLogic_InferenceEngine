import java.util.HashSet;

public class Sentence {
		HashSet<Term> terms;

		public Sentence(HashSet<Term> terms) {
			super();
			this.terms = terms;
		}

		public Sentence() {
			terms = new HashSet<>();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((terms == null) ? 0 : terms.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Sentence other = (Sentence) obj;
			if (terms == null) {
				if (other.terms != null)
					return false;
			} else if (!terms.equals(other.terms))
				return false;
			return true;
		}
	}