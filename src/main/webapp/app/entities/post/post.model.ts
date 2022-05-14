import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';

export interface IPost {
  id?: string;
  title?: string | null;
  required?: string | null;
  creationDate?: dayjs.Dayjs | null;
  creator?: IUser;
}

export class Post implements IPost {
  constructor(
    public id?: string,
    public title?: string | null,
    public required?: string | null,
    public creationDate?: dayjs.Dayjs | null,
    public creator?: IUser
  ) {}
}

export function getPostIdentifier(post: IPost): string | undefined {
  return post.id;
}
